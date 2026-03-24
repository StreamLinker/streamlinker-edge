package io.streamlinker.edge.service.task;

import io.streamlinker.edge.domain.ExpectedState;
import io.streamlinker.edge.domain.StreamState;
import io.streamlinker.edge.infra.db.entity.StreamEntity;
import io.streamlinker.edge.infra.db.entity.StreamRuntimeEntity;
import io.streamlinker.edge.infra.db.repository.StreamRepository;
import io.streamlinker.edge.infra.db.repository.StreamRuntimeRepository;
import io.streamlinker.edge.service.PullRuntimeProbe;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@ConditionalOnProperty(prefix = "streamlinker.edge.reconcile", name = "runtime-sync-enabled", havingValue = "true", matchIfMissing = true)
public class RuntimeSyncTask {

    private final StreamRepository streamRepository;
    private final StreamRuntimeRepository streamRuntimeRepository;
    private final PullRuntimeProbe pullRuntimeProbe;

    public RuntimeSyncTask(StreamRepository streamRepository,
                           StreamRuntimeRepository streamRuntimeRepository,
                           PullRuntimeProbe pullRuntimeProbe) {
        this.streamRepository = streamRepository;
        this.streamRuntimeRepository = streamRuntimeRepository;
        this.pullRuntimeProbe = pullRuntimeProbe;
    }

    @Scheduled(fixedDelayString = "${streamlinker.edge.reconcile.runtime-sync-interval-ms:3000}")
    public void syncAll() {
        for (StreamEntity stream : streamRepository.findAll()) {
            StreamRuntimeEntity runtime = streamRuntimeRepository.findByStreamId(stream.getId());
            if (runtime == null) {
                continue;
            }
            boolean online = pullRuntimeProbe.isOnline(stream);
            runtime.setLocalOnline(online ? 1 : 0);
            runtime.setPullStatus(resolvePullStatus(stream, online));
            runtime.setLastError(online ? null : "Pull runtime probe reported offline");
            runtime.setLastOnlineAt(online ? LocalDateTime.now() : runtime.getLastOnlineAt());
            runtime.setUpdateTime(LocalDateTime.now());
            streamRuntimeRepository.saveOrUpdate(runtime);
        }
    }

    private String resolvePullStatus(StreamEntity stream, boolean online) {
        if (online) {
            return StreamState.RUNNING.name();
        }
        if (ExpectedState.RUNNING.name().equals(stream.getExpectedState())) {
            return StreamState.FAILED.name();
        }
        return StreamState.IDLE.name();
    }
}