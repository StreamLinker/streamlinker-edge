package io.streamlinker.edge.web.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PushTargetRuntimeView {
    private Long pushTargetId;
    private Long streamId;
    private String streamCode;
    private String targetCode;
    private String targetName;
    private String pushStatus;
    private Integer online;
    private String pusherKey;
    private String lastError;
    private LocalDateTime lastOnlineAt;
    private LocalDateTime updateTime;
}