package org.apache.dolphinscheduler.plugin.task.guicang.rpc;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author huangxiaoli
 * @create 2023/10/19 10:12
 */
public class JobFuture {

    private boolean success = false;

    private StringBuilder taskInfo = new StringBuilder("\n");

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public StringBuilder getTaskInfo() {
        return taskInfo;
    }

    public void setTaskInfo(StringBuilder taskInfo) {
        this.taskInfo = taskInfo;
    }

    public void info(String info) {
        this.taskInfo.append("[INFO] ")
                .append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS Z").format(new Date()))
                .append(" - ").append(info)
                .append("\n");
    }

    public void error(String info) {
        this.taskInfo.append("[ERROR] ")
                .append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS Z").format(new Date()))
                .append(" - ").append(info)
                .append("\n");
    }

    public String finallyInfo() {
        return taskInfo.toString();
    }

}
