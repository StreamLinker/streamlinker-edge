package io.streamlinker.edge.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StreamUpsertRequest {

    @NotBlank
    private String streamCode;

    @NotBlank
    private String name;

    @NotBlank
    private String sourceUrl;

    @NotBlank
    private String sourceProtocol;

    @NotBlank
    private String accessMode;

    @NotBlank
    private String localApp;

    @NotBlank
    private String localStream;

    @NotNull
    private Integer enabled;

    @NotBlank
    private String expectedState;

    private String remark;
}