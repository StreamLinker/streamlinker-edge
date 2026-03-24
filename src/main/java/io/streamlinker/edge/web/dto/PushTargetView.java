package io.streamlinker.edge.web.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PushTargetView {
    private Long id;
    private Long streamId;
    private String targetCode;
    private String targetName;
    private String targetType;
    private String targetProtocol;
    private String targetUrl;
    private String targetApp;
    private String targetStream;
    private Integer enabled;
    private String expectedState;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}