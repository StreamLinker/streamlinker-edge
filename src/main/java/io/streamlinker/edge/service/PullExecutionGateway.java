package io.streamlinker.edge.service;

import io.streamlinker.edge.domain.StreamDefinition;
import io.streamlinker.edge.domain.StreamRuntime;
import io.streamlinker.edge.infra.db.entity.StreamEntity;

public interface PullExecutionGateway {

    StreamRuntime startFfmpeg(StreamDefinition definition, StreamRuntime currentRuntime);

    StreamRuntime startProxy(StreamDefinition definition, StreamRuntime currentRuntime);

    StreamRuntime stop(StreamDefinition definition, StreamRuntime currentRuntime);

    boolean isOnline(StreamEntity stream);
}