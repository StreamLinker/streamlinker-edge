package io.streamlinker.edge.infra.db.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("stream_process")
public class StreamProcessEntity {

    @TableId
    private Long id;
    private String processType;
    private Long streamId;
    private Long pushTargetId;
    private Integer step;
    private String status;
    private Integer retryCount;
    private Integer maxRetryCount;
    private String requestSnapshot;
    private String contextSnapshot;
    private String errorMessage;
    private LocalDateTime startTime;
    private LocalDateTime finishTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}