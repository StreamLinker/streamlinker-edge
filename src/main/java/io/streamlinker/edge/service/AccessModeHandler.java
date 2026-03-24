package io.streamlinker.edge.service;

import io.streamlinker.edge.domain.StreamDefinition;
import io.streamlinker.edge.domain.StreamRuntime;

public interface AccessModeHandler {
    boolean supports(StreamDefinition definition);

    StreamRuntime start(StreamDefinition definition, StreamRuntime currentRuntime);

    StreamRuntime stop(StreamDefinition definition, StreamRuntime currentRuntime);
}