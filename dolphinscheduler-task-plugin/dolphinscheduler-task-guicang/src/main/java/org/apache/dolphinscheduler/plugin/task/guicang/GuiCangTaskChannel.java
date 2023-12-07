package org.apache.dolphinscheduler.plugin.task.guicang;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.AbstractTask;
import org.apache.dolphinscheduler.plugin.task.api.TaskChannel;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.ParametersNode;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.ResourceParametersHelper;

/**
 * @author huangxiaoli
 * @create 2023/10/18 10:16
 */
public class GuiCangTaskChannel implements TaskChannel {

    @Override
    public void cancelApplication(boolean status) {
        // nothing to do
    }

    @Override
    public AbstractTask createTask(TaskExecutionContext taskRequest) {
        return new GuiCangTask(taskRequest);
    }

    @Override
    public AbstractParameters parseParameters(ParametersNode parametersNode) {
        return JSONUtils.parseObject(parametersNode.getTaskParams(), GuiCangParameters.class);
    }

    @Override
    public ResourceParametersHelper getResources(String parameters) {
        return null;
    }

}
