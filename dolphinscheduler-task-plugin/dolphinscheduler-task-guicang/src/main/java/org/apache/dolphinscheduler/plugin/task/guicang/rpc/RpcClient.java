package org.apache.dolphinscheduler.plugin.task.guicang.rpc;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.guicang.GuiCangParameters;
import org.apache.dolphinscheduler.plugin.task.guicang.rpc.message.MessageType;
import org.apache.dolphinscheduler.plugin.task.guicang.rpc.message.RemoteMessage;

import java.util.Objects;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;

/**
 * @author huangxiaoli
 * @create 2023/10/19 9:51
 */
public class RpcClient {

    private final TaskExecutionContext ctx;

    private final String systemTaskInstanceId;

    private final GuiCangParameters parameters;

    private Channel channel;

    public RpcClient(TaskExecutionContext ctx,
                     String systemTaskInstanceId,
                     GuiCangParameters parameters) {
        this.ctx = ctx;
        this.systemTaskInstanceId = systemTaskInstanceId;
        this.parameters = parameters;
    }

    public JobFuture start() throws InterruptedException {
        JobFuture future = new JobFuture();
        NioEventLoopGroup group = new NioEventLoopGroup(1);
        try {
            Bootstrap bootstrap = new Bootstrap()
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                    .addLast(new RpcClientHandler(future, ctx, systemTaskInstanceId,
                                            parameters));
                        }
                    });

            ChannelFuture sync = bootstrap.connect(parameters.getNettyServerHost(), Integer
                    .parseInt(parameters.getNettyServerPort())).sync();

            this.channel = sync.channel();

            sync.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }

        return future;
    }

    public void close() {
        this.channel.close();
    }
}

class RpcClientHandler extends ChannelInboundHandlerAdapter {

    private final JobFuture future;

    private final TaskExecutionContext taskRequest;

    private final String systemTaskInstanceId;

    private final GuiCangParameters parameters;

    RpcClientHandler(JobFuture future, TaskExecutionContext taskRequest,
                     String systemTaskInstanceId, GuiCangParameters parameters) {
        this.future = future;
        this.taskRequest = taskRequest;
        this.systemTaskInstanceId = systemTaskInstanceId;
        this.parameters = parameters;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        String msgJson = buf.toString(CharsetUtil.UTF_8);

        RemoteMessage remoteMessage = JSONUtils.parseObject(msgJson, RemoteMessage.class);

        switch (Objects.requireNonNull(remoteMessage).getMessageType()) {
            case FAILED:
                future.setSuccess(false);
                future.error(String.format("收到回调消息 %s 任务失败, 通道即将关闭", remoteMessage));
                ctx.channel().close();
                break;
            case SUCCESS:
                future.setSuccess(true);
                future.info(String.format("收到回调消息 %s 任务成功, 通道即将关闭", remoteMessage));
                ctx.channel().close();
                break;
            default:
                ctx.channel().close();
                throw new IllegalArgumentException("非法参数 :" + remoteMessage.getMessageType());
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {

        RemoteMessage message = buildRegisterMessage();

        ctx.writeAndFlush(
                Unpooled.copiedBuffer(JSONUtils.toJsonString(message), CharsetUtil.UTF_8));

        future.info(
                String.format("通道开启, 向服务端: %s:%s 进行注册, 注册信息: %s", parameters.getNettyServerHost(),
                        parameters.getNettyServerPort(), message));

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();

        future.error(cause.getMessage());
    }

    public RemoteMessage buildRegisterMessage() {

        return RemoteMessage.builder()
                .commandType(taskRequest.getCmdTypeIfComplement())
                .processDefinitionCode(taskRequest.getProcessDefineCode())
                .schedulerProcessInstanceId(taskRequest.getProcessInstanceId())
                .schedulerTaskInstanceId(taskRequest.getTaskInstanceId())
                .endTime(taskRequest.getEndTime())
                .schedulerTaskInstanceName(taskRequest.getTaskName())
                .startTime(taskRequest.getStartTime())
                .messageType(MessageType.REGISTER)
                .systemTaskInstanceId(systemTaskInstanceId)
                .build();

    }

}
