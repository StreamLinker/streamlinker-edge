package io.streamlinker.edge.service;

import io.streamlinker.edge.domain.AccessMode;
import io.streamlinker.edge.domain.StreamDefinition;
import io.streamlinker.edge.domain.StreamRuntime;
import org.springframework.stereotype.Component;

@Component
public class FfmpegPullPushHandler implements AccessModeHandler {

    private final PullExecutionGateway pullExecutionGateway;

    public FfmpegPullPushHandler(PullExecutionGateway pullExecutionGateway) {
        this.pullExecutionGateway = pullExecutionGateway;
    }

    @Override
    public boolean supports(StreamDefinition definition) {
        return definition.getAccessMode() == AccessMode.FFMPEG;
    }

    @Override
    public StreamRuntime start(StreamDefinition definition, StreamRuntime currentRuntime) {
        return pullExecutionGateway.startFfmpeg(definition, currentRuntime);
    }

    @Override
    public StreamRuntime stop(StreamDefinition definition, StreamRuntime currentRuntime) {
        return pullExecutionGateway.stop(definition, currentRuntime);
    }
}