package io.streamlinker.edge.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StreamRuntime {
    private String streamId;
    private StreamState state;
    private AccessMode accessMode;
    private String zlmTaskKey;
    private String proxyKey;
    private String pusherKey;
    private Boolean localOnline;
    private Boolean cloudOnline;
    private String lastError;
    private Instant updatedAt;
}
