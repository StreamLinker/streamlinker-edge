package io.streamlinker.edge.web.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class StreamView {
    private Long id;
    private String streamCode;
    private String name;
    private String sourceUrl;
    private String sourceProtocol;
    private String accessMode;
    private String localApp;
    private String localStream;
    private Integer enabled;
    private String expectedState;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}