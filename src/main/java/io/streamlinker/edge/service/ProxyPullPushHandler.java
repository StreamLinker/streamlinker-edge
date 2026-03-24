package io.streamlinker.edge.service;

import io.streamlinker.edge.domain.AccessMode;
import io.streamlinker.edge.domain.StreamDefinition;
import io.streamlinker.edge.domain.StreamRuntime;
import org.springframework.stereotype.Component;

@Component
public class ProxyPullPushHandler implements AccessModeHandler {

    private final PullExecutionGateway pullExecutionGateway;

    public ProxyPullPushHandler(PullExecutionGateway pullExecutionGateway) {
        this.pullExecutionGateway = pullExecutionGateway;
    }

    @Override
    public boolean supports(StreamDefinition definition) {
        return definition.getAccessMode() == AccessMode.PROXY;
    }

    @Override
    public StreamRuntime start(StreamDefinition definition, StreamRuntime currentRuntime) {
        return pullExecutionGateway.startProxy(definition, currentRuntime);
    }

    @Override
    public StreamRuntime stop(StreamDefinition definition, StreamRuntime currentRuntime) {
        return pullExecutionGateway.stop(definition, currentRuntime);
    }
}