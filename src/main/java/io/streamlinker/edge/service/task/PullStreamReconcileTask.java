package io.streamlinker.edge.service.task;

import io.streamlinker.edge.domain.ExpectedState;
import io.streamlinker.edge.domain.StreamState;
import io.streamlinker.edge.infra.db.entity.StreamEntity;
import io.streamlinker.edge.infra.db.entity.StreamRuntimeEntity;
import io.streamlinker.edge.infra.db.repository.StreamRepository;
import io.streamlinker.edge.infra.db.repository.StreamRuntimeRepository;
import io.streamlinker.edge.service.StreamOrchestrator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "streamlinker.edge.reconcile", name = "pull-reconcile-enabled", havingValue = "true", matchIfMissing = true)
public class PullStreamReconcileTask {

    private final StreamRepository streamRepository;
    private final StreamRuntimeRepository streamRuntimeRepository;
    private final StreamOrchestrator streamOrchestrator;

    public PullStreamReconcileTask(StreamRepository streamRepository,
                                   StreamRuntimeRepository streamRuntimeRepository,
                                   StreamOrchestrator streamOrchestrator) {
        this.streamRepository = streamRepository;
        this.streamRuntimeRepository = streamRuntimeRepository;
        this.streamOrchestrator = streamOrchestrator;
    }

    @Scheduled(fixedDelayString = "${streamlinker.edge.reconcile.pull-interval-ms:5000}")
    public void reconcileAll() {
        reconcileExpectedRunning();
        reconcileExpectedStopped();
    }

    public void reconcileExpectedRunning() {
        for (StreamEntity stream : streamRepository.findActiveByExpectedState(ExpectedState.RUNNING.name())) {
            StreamRuntimeEntity runtime = streamRuntimeRepository.findByStreamId(stream.getId());
            if (runtime == null || runtime.getLocalOnline() == null || runtime.getLocalOnline() == 0 || !StreamState.RUNNING.name().equals(runtime.getPullStatus())) {
                streamOrchestrator.start(stream.getStreamCode());
            }
        }
    }

    public void reconcileExpectedStopped() {
        for (StreamEntity stream : streamRepository.findActiveByExpectedState(ExpectedState.STOPPED.name())) {
            StreamRuntimeEntity runtime = streamRuntimeRepository.findByStreamId(stream.getId());
            if (runtime == null) {
                continue;
            }
            if (runtime.getLocalOnline() != null && runtime.getLocalOnline() == 1) {
                streamOrchestrator.stop(stream.getStreamCode());
            }
        }
    }
}