package io.streamlinker.edge.service.task;

import io.streamlinker.edge.domain.ExpectedState;
import io.streamlinker.edge.infra.db.entity.StreamPushRuntimeEntity;
import io.streamlinker.edge.infra.db.entity.StreamPushTargetEntity;
import io.streamlinker.edge.infra.db.repository.StreamPushRuntimeRepository;
import io.streamlinker.edge.infra.db.repository.StreamPushTargetRepository;
import io.streamlinker.edge.service.PushTargetOrchestrator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "streamlinker.edge.reconcile", name = "push-reconcile-enabled", havingValue = "true", matchIfMissing = false)
public class PushTargetReconcileTask {

    private final StreamPushTargetRepository streamPushTargetRepository;
    private final StreamPushRuntimeRepository streamPushRuntimeRepository;
    private final PushTargetOrchestrator pushTargetOrchestrator;

    public PushTargetReconcileTask(StreamPushTargetRepository streamPushTargetRepository,
                                   StreamPushRuntimeRepository streamPushRuntimeRepository,
                                   PushTargetOrchestrator pushTargetOrchestrator) {
        this.streamPushTargetRepository = streamPushTargetRepository;
        this.streamPushRuntimeRepository = streamPushRuntimeRepository;
        this.pushTargetOrchestrator = pushTargetOrchestrator;
    }

    @Scheduled(fixedDelayString = "${streamlinker.edge.reconcile.push-interval-ms:5000}")
    public void reconcileAll() {
        reconcileExpectedRunning();
        reconcileExpectedStopped();
    }

    public void reconcileExpectedRunning() {
        for (StreamPushTargetEntity target : streamPushTargetRepository.findActiveByExpectedState(ExpectedState.RUNNING.name())) {
            StreamPushRuntimeEntity runtime = streamPushRuntimeRepository.findByPushTargetId(target.getId());
            if (runtime == null || runtime.getOnline() == null || runtime.getOnline() == 0) {
                pushTargetOrchestrator.start(target.getTargetCode());
            }
        }
    }

    public void reconcileExpectedStopped() {
        for (StreamPushTargetEntity target : streamPushTargetRepository.findActiveByExpectedState(ExpectedState.STOPPED.name())) {
            StreamPushRuntimeEntity runtime = streamPushRuntimeRepository.findByPushTargetId(target.getId());
            if (runtime == null) {
                continue;
            }
            if (runtime.getOnline() != null && runtime.getOnline() == 1) {
                pushTargetOrchestrator.stop(target.getTargetCode());
            }
        }
    }
}