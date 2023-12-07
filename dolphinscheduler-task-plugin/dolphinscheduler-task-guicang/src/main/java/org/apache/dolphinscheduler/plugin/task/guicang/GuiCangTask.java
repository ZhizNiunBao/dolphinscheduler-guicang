package org.apache.dolphinscheduler.plugin.task.guicang;

import org.apache.dolphinscheduler.common.enums.HttpMethod;
import org.apache.dolphinscheduler.common.enums.HttpParametersType;
import org.apache.dolphinscheduler.common.process.HttpProperty;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.AbstractTask;
import org.apache.dolphinscheduler.plugin.task.api.TaskCallBack;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.plugin.task.api.parser.ParamUtils;
import org.apache.dolphinscheduler.plugin.task.api.parser.ParameterUtils;
import org.apache.dolphinscheduler.plugin.task.guicang.rpc.JobFuture;
import org.apache.dolphinscheduler.plugin.task.guicang.rpc.RpcClient;
import org.apache.dolphinscheduler.plugin.task.guicang.util.SingleResult;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.Charsets;
import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author huangxiaoli
 * @create 2023/10/18 10:17
 */
public class GuiCangTask extends AbstractTask {

    /**
     * guicang parameters
     */
    private GuiCangParameters parameters;

    /**
     * rpc client
     */
    private RpcClient client;

    /**
     * constructor
     *
     * @param taskExecutionContext taskExecutionContext
     */
    protected GuiCangTask(
                          TaskExecutionContext taskExecutionContext) {
        super(taskExecutionContext);
    }

    @Override
    public void handle(TaskCallBack taskCallBack) throws TaskException {

        String taskInstanceId = sendRequest();

        JobFuture future = null;
        RpcClient rpcClient = new RpcClient(taskRequest, taskInstanceId, parameters);
        try {
            future = rpcClient.start();
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new GuiCangTaskException("监听失败");
        }

        if (!future.isSuccess()) {
            setExitStatusCode(-1);
            logger.info("任务失败, 任务执行信息: {}", future.finallyInfo());
        } else {
            logger.info("任务成功, 任务执行信息: {}", future.finallyInfo());
            setExitStatusCode(0);
        }

    }

    public String sendRequest() {
        long startTime = System.currentTimeMillis();
        String formatTimeStamp = DateUtils.formatTimeStamp(startTime);
        String statusCode = null;
        String body = null;

        try (
            CloseableHttpClient client = createHttpClient();
            CloseableHttpResponse response = sendRequest(client)) {
            statusCode = String.valueOf(getStatusCode(response));
            body = getResponseBody(response);
            long costTime = System.currentTimeMillis() - startTime;
            logger.info(
                "startTime: {}, httpUrl: {}, httpMethod: {}, costTime : {} milliseconds, statusCode : {}, body : {}",
                formatTimeStamp, parameters.getUrl(),
                parameters.getHttpMethod(), costTime, statusCode, body);
        } catch (Exception e) {
            exitStatusCode = -1;
            logger.error("httpUrl[" + parameters.getUrl() + "] connection failed：", e);
            throw new TaskException("Execute http task failed", e);
        }

        logger.info("responseBody {}", body);

        logger.info("processInstanceId: {}", taskRequest.getProcessInstanceId());

        SingleResult<String> singleResult = JSONUtils.parseObject(body, SingleResult.class);
        assert singleResult != null;

        if (!singleResult.isSuccess() || singleResult.getData() == null) {
            throw new IllegalArgumentException("请求失败");
        }
        return singleResult.getData();
    }

    @Override
    public void cancel() throws TaskException {
        this.client.close();
        logger.info("task kill current thread: {}", Thread.currentThread());
    }

    @Override
    public AbstractParameters getParameters() {
        return this.parameters;
    }

    @Override
    public void init() {
        final String taskParams = taskRequest.getTaskParams();
        logger.info("guicang task params:{}", taskParams);
        this.parameters = JSONUtils.parseObject(taskParams, GuiCangParameters.class);
    }

    protected CloseableHttpClient createHttpClient() {
        HttpClientBuilder httpClientBuilder;
        httpClientBuilder = HttpClients.custom();
        return httpClientBuilder.build();
    }

    /**
     * send request
     *
     * @param client client
     * @return CloseableHttpResponse
     * @throws IOException io exception
     */
    protected CloseableHttpResponse sendRequest(CloseableHttpClient client) throws IOException {
        RequestBuilder builder = createRequestBuilder();

        // replace placeholder,and combine local and global parameters
        Map<String, Property> paramsMap = taskRequest.getPrepareParamsMap();

        List<HttpProperty> httpPropertyList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(parameters.getHttpParams())) {
            for (HttpProperty httpProperty : parameters.getHttpParams()) {
                String jsonObject = JSONUtils.toJsonString(httpProperty);
                String params = ParameterUtils
                        .convertParameterPlaceholders(jsonObject, ParamUtils.convert(paramsMap));
                logger.info("http request params：{}", params);
                httpPropertyList.add(JSONUtils.parseObject(params, HttpProperty.class));
            }
        }
        addRequestParams(builder, httpPropertyList);
        String requestUrl =
                ParameterUtils.convertParameterPlaceholders(parameters.getUrl(), ParamUtils.convert(paramsMap));
        HttpUriRequest request = builder.setUri(requestUrl).build();
        setHeaders(request, httpPropertyList);
        return client.execute(request);
    }

    protected RequestBuilder createRequestBuilder() {
        if (parameters.getHttpMethod().equals(HttpMethod.GET)) {
            return RequestBuilder.get();
        } else if (parameters.getHttpMethod().equals(HttpMethod.POST)) {
            return RequestBuilder.post();
        } else if (parameters.getHttpMethod().equals(HttpMethod.HEAD)) {
            return RequestBuilder.head();
        } else if (parameters.getHttpMethod().equals(HttpMethod.PUT)) {
            return RequestBuilder.put();
        } else if (parameters.getHttpMethod().equals(HttpMethod.DELETE)) {
            return RequestBuilder.delete();
        } else {
            return null;
        }
    }

    protected void setHeaders(HttpUriRequest request, List<HttpProperty> httpPropertyList) {
        if (CollectionUtils.isNotEmpty(httpPropertyList)) {
            for (HttpProperty property : httpPropertyList) {
                if (HttpParametersType.HEADERS.equals(property.getHttpParametersType())) {
                    request.addHeader(property.getProp(), property.getValue());
                }
            }
        }
    }

    protected String getResponseBody(CloseableHttpResponse httpResponse) throws ParseException, IOException {
        if (httpResponse == null) {
            return null;
        }
        HttpEntity entity = httpResponse.getEntity();
        if (entity == null) {
            return null;
        }
        return EntityUtils.toString(entity, StandardCharsets.UTF_8.name());
    }

    protected int getStatusCode(CloseableHttpResponse httpResponse) {
        return httpResponse.getStatusLine().getStatusCode();
    }

    protected void addRequestParams(RequestBuilder builder, List<HttpProperty> httpPropertyList) {
        if (CollectionUtils.isNotEmpty(httpPropertyList)) {
            ObjectNode jsonParam = JSONUtils.createObjectNode();
            for (HttpProperty property : httpPropertyList) {
                if (property.getHttpParametersType() != null) {
                    if (property.getHttpParametersType().equals(HttpParametersType.PARAMETER)) {
                        builder.addParameter(property.getProp(), property.getValue());
                    } else if (property.getHttpParametersType().equals(HttpParametersType.BODY)) {
                        jsonParam.put(property.getProp(), property.getValue());
                    }
                }
            }
            StringEntity postingString = new StringEntity(jsonParam.toString(), Charsets.UTF_8);
            postingString.setContentEncoding(StandardCharsets.UTF_8.name());
            postingString.setContentType("application/json");
            builder.setEntity(postingString);
        }
    }

}
