package io.streamlinker.edge.service.task;

import io.streamlinker.edge.domain.ExpectedState;
import io.streamlinker.edge.domain.PushStatus;
import io.streamlinker.edge.infra.db.entity.StreamPushRuntimeEntity;
import io.streamlinker.edge.infra.db.entity.StreamPushTargetEntity;
import io.streamlinker.edge.infra.db.repository.StreamPushRuntimeRepository;
import io.streamlinker.edge.infra.db.repository.StreamPushTargetRepository;
import io.streamlinker.edge.service.PushRuntimeProbe;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@ConditionalOnProperty(prefix = "streamlinker.edge.reconcile", name = "runtime-sync-enabled", havingValue = "true", matchIfMissing = true)
public class PushRuntimeSyncTask {

    private final StreamPushTargetRepository streamPushTargetRepository;
    private final StreamPushRuntimeRepository streamPushRuntimeRepository;
    private final PushRuntimeProbe pushRuntimeProbe;

    public PushRuntimeSyncTask(StreamPushTargetRepository streamPushTargetRepository,
                               StreamPushRuntimeRepository streamPushRuntimeRepository,
                               PushRuntimeProbe pushRuntimeProbe) {
        this.streamPushTargetRepository = streamPushTargetRepository;
        this.streamPushRuntimeRepository = streamPushRuntimeRepository;
        this.pushRuntimeProbe = pushRuntimeProbe;
    }

    @Scheduled(fixedDelayString = "${streamlinker.edge.reconcile.runtime-sync-interval-ms:3000}")
    public void syncAll() {
        for (StreamPushTargetEntity target : streamPushTargetRepository.findAll()) {
            StreamPushRuntimeEntity runtime = streamPushRuntimeRepository.findByPushTargetId(target.getId());
            if (runtime == null) {
                continue;
            }
            boolean online = pushRuntimeProbe.isOnline(target);
            runtime.setOnline(online ? 1 : 0);
            runtime.setPushStatus(resolvePushStatus(target, online));
            runtime.setLastError(online ? null : "Push runtime probe reported offline");
            runtime.setLastOnlineAt(online ? LocalDateTime.now() : runtime.getLastOnlineAt());
            runtime.setUpdateTime(LocalDateTime.now());
            streamPushRuntimeRepository.saveOrUpdate(runtime);
        }
    }

    private String resolvePushStatus(StreamPushTargetEntity target, boolean online) {
        if (online) {
            return PushStatus.RUNNING.name();
        }
        if (ExpectedState.RUNNING.name().equals(target.getExpectedState())) {
            return PushStatus.FAILED.name();
        }
        return PushStatus.IDLE.name();
    }
}