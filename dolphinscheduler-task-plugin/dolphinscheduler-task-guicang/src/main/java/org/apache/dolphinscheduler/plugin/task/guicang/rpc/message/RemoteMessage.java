package org.apache.dolphinscheduler.plugin.task.guicang.rpc.message;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author huangxiaoli
 * @create 2023/10/24 16:27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RemoteMessage {

    private MessageType messageType;

    private String systemTaskInstanceId;

    private String schedulerTaskInstanceName;

    private Integer schedulerProcessInstanceId;

    private Integer schedulerTaskInstanceId;

    private Long processDefinitionCode;

    private Integer commandType;

    private Date startTime;

    private Date endTime;

}
