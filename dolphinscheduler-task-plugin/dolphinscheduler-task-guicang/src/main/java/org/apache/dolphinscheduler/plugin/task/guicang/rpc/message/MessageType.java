package org.apache.dolphinscheduler.plugin.task.guicang.rpc.message;

/**
 * @author huangxiaoli
 * @create 2023/10/24 16:29
 */
public enum MessageType {
    /**
     * client注册任务信息类型
     */
    REGISTER,

    /**
     * server通知client任务失败
     */
    FAILED,

    /**
     * server通知client任务成功
     */
    SUCCESS
}
