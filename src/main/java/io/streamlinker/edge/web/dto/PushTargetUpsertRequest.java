package io.streamlinker.edge.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PushTargetUpsertRequest {

    @NotNull
    private Long streamId;

    @NotBlank
    private String targetCode;

    @NotBlank
    private String targetName;

    @NotBlank
    private String targetType;

    @NotBlank
    private String targetProtocol;

    @NotBlank
    private String targetUrl;

    private String targetApp;

    private String targetStream;

    @NotNull
    private Integer enabled;

    @NotBlank
    private String expectedState;

    private String remark;
}