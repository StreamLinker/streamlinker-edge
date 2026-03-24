package io.streamlinker.edge.service.task;

import io.streamlinker.edge.domain.ExpectedState;
import io.streamlinker.edge.infra.db.entity.StreamEntity;
import io.streamlinker.edge.infra.db.repository.StreamRepository;
import io.streamlinker.edge.service.PullRuntimeProbe;
import io.streamlinker.edge.service.StreamOrchestrator;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "streamlinker.edge.reconcile", name = "startup-recover-enabled", havingValue = "true", matchIfMissing = true)
public class EdgeStartupRecoverTask implements ApplicationRunner {

    private final StreamRepository streamRepository;
    private final PullRuntimeProbe pullRuntimeProbe;
    private final StreamOrchestrator streamOrchestrator;

    public EdgeStartupRecoverTask(StreamRepository streamRepository,
                                  PullRuntimeProbe pullRuntimeProbe,
                                  StreamOrchestrator streamOrchestrator) {
        this.streamRepository = streamRepository;
        this.pullRuntimeProbe = pullRuntimeProbe;
        this.streamOrchestrator = streamOrchestrator;
    }

    @Override
    public void run(ApplicationArguments args) {
        recoverExpectedRunningStreams();
    }

    public void recoverExpectedRunningStreams() {
        for (StreamEntity stream : streamRepository.findActiveByExpectedState(ExpectedState.RUNNING.name())) {
            if (pullRuntimeProbe.isOnline(stream)) {
                continue;
            }
            streamOrchestrator.start(stream.getStreamCode());
        }
    }
}