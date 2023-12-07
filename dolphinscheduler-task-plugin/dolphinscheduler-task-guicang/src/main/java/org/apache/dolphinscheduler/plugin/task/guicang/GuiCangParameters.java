package org.apache.dolphinscheduler.plugin.task.guicang;

import org.apache.dolphinscheduler.common.enums.HttpMethod;
import org.apache.dolphinscheduler.common.process.HttpProperty;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;

import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Objects;

/**
 * @author huangxiaoli
 * @create 2023/10/18 10:05
 */
public class GuiCangParameters extends AbstractParameters {

    /**
     * parameters for guicang Open API
     */
    private HttpMethod httpMethod;

    private List<HttpProperty> httpParams;

    private String url;

    private String nettyServerHost;

    private String nettyServerPort;

    public GuiCangParameters() {
    }

    @Override
    public boolean checkParameters() {
        return StringUtils.isNotEmpty(this.url) && httpMethod != null;
    }

    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(HttpMethod httpMethod) {
        this.httpMethod = httpMethod;
    }

    public List<HttpProperty> getHttpParams() {
        return httpParams;
    }

    public void setHttpParams(
                              List<HttpProperty> httpParams) {
        this.httpParams = httpParams;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getNettyServerHost() {
        return nettyServerHost;
    }

    public void setNettyServerHost(String nettyServerHost) {
        this.nettyServerHost = nettyServerHost;
    }

    public String getNettyServerPort() {
        return nettyServerPort;
    }

    public void setNettyServerPort(String nettyServerPort) {
        this.nettyServerPort = nettyServerPort;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GuiCangParameters that = (GuiCangParameters) o;
        return httpMethod == that.httpMethod && Objects.equals(httpParams, that.httpParams)
                && url.equals(that.url) && nettyServerHost.equals(that.nettyServerHost)
                && nettyServerPort
                        .equals(that.nettyServerPort);
    }

    @Override
    public int hashCode() {
        return Objects.hash(httpMethod, httpParams, url);
    }

    @Override
    public String toString() {
        return "GuiCangParameters{" +
                "httpMethod=" + httpMethod +
                ", httpParams=" + httpParams +
                ", url='" + url + '\'' +
                ", nettyServerHost='" + nettyServerHost + '\'' +
                ", nettyServerPort='" + nettyServerPort + '\'' +
                '}';
    }

}
