package io.streamlinker.edge.infra.db.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("stream_push_runtime")
public class StreamPushRuntimeEntity {

    @TableId
    private Long pushTargetId;
    private String pushStatus;
    private String pusherKey;
    private Integer online;
    private LocalDateTime lastOnlineAt;
    private String lastError;
    private Long reconcileVersion;
    private LocalDateTime updateTime;
}