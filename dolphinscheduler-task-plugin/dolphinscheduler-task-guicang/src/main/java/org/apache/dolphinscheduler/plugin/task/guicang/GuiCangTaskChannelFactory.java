package org.apache.dolphinscheduler.plugin.task.guicang;

import org.apache.dolphinscheduler.plugin.task.api.TaskChannel;
import org.apache.dolphinscheduler.plugin.task.api.TaskChannelFactory;
import org.apache.dolphinscheduler.spi.params.base.PluginParams;

import java.util.ArrayList;
import java.util.List;

import com.google.auto.service.AutoService;

/**
 * @author huangxiaoli
 * @create 2023/10/18 10:14
 */
@AutoService(TaskChannelFactory.class)
public class GuiCangTaskChannelFactory implements TaskChannelFactory {

    @Override
    public String getName() {
        return "GUICANG";
    }

    @Override
    public List<PluginParams> getParams() {
        return new ArrayList<>();
    }

    @Override
    public TaskChannel create() {
        return new GuiCangTaskChannel();
    }

}
