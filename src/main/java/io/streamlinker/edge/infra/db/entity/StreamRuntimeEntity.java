package io.streamlinker.edge.infra.db.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("stream_runtime")
public class StreamRuntimeEntity {

    @TableId
    private Long streamId;
    private String pullStatus;
    private String mediaKey;
    private String zlmTaskKey;
    private Integer localOnline;
    private LocalDateTime lastOnlineAt;
    private String lastError;
    private Long reconcileVersion;
    private LocalDateTime updateTime;
}