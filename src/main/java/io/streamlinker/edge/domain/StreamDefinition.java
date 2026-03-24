package io.streamlinker.edge.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StreamDefinition {
    private String streamId;
    private String edgeId;
    private String name;
    private Boolean enabled;
    private AccessMode accessMode;
    private String sourceUrl;
    private String localApp;
    private String localStream;
    private String cloudPushUrl;
    private String cloudApp;
    private String cloudStream;
    private Boolean autoStart;
    private String remark;
}
