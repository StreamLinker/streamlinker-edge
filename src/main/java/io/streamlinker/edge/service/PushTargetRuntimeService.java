package io.streamlinker.edge.service;

import io.streamlinker.edge.infra.db.entity.StreamEntity;
import io.streamlinker.edge.infra.db.entity.StreamPushRuntimeEntity;
import io.streamlinker.edge.infra.db.entity.StreamPushTargetEntity;
import io.streamlinker.edge.infra.db.repository.StreamPushRuntimeRepository;
import io.streamlinker.edge.infra.db.repository.StreamPushTargetRepository;
import io.streamlinker.edge.infra.db.repository.StreamRepository;
import io.streamlinker.edge.web.dto.PushTargetRuntimeView;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PushTargetRuntimeService {

    private final StreamPushTargetRepository streamPushTargetRepository;
    private final StreamPushRuntimeRepository streamPushRuntimeRepository;
    private final StreamRepository streamRepository;

    public PushTargetRuntimeService(StreamPushTargetRepository streamPushTargetRepository,
                                    StreamPushRuntimeRepository streamPushRuntimeRepository,
                                    StreamRepository streamRepository) {
        this.streamPushTargetRepository = streamPushTargetRepository;
        this.streamPushRuntimeRepository = streamPushRuntimeRepository;
        this.streamRepository = streamRepository;
    }

    public List<PushTargetRuntimeView> list() {
        Map<Long, StreamEntity> streamsById = streamRepository.findAll().stream()
                .collect(Collectors.toMap(StreamEntity::getId, Function.identity()));
        Map<Long, StreamPushRuntimeEntity> runtimeByTargetId = streamPushRuntimeRepository.findAll().stream()
                .collect(Collectors.toMap(StreamPushRuntimeEntity::getPushTargetId, Function.identity()));
        return streamPushTargetRepository.findAll().stream()
                .filter(target -> target.getDeleted() == null || target.getDeleted() == 0)
                .sorted(Comparator.comparing(StreamPushTargetEntity::getTargetCode))
                .map(target -> toView(target, runtimeByTargetId.get(target.getId()), streamsById.get(target.getStreamId())))
                .toList();
    }

    public List<PushTargetRuntimeView> listByStreamCode(String streamCode) {
        StreamEntity stream = streamRepository.findByStreamId(streamCode);
        if (stream == null) {
            throw new IllegalArgumentException("Unknown streamCode: " + streamCode);
        }
        Map<Long, StreamPushRuntimeEntity> runtimeByTargetId = streamPushRuntimeRepository.findAll().stream()
                .collect(Collectors.toMap(StreamPushRuntimeEntity::getPushTargetId, Function.identity()));
        return streamPushTargetRepository.findAll().stream()
                .filter(target -> stream.getId().equals(target.getStreamId()))
                .filter(target -> target.getDeleted() == null || target.getDeleted() == 0)
                .sorted(Comparator.comparing(StreamPushTargetEntity::getTargetCode))
                .map(target -> toView(target, runtimeByTargetId.get(target.getId()), stream))
                .toList();
    }

    private PushTargetRuntimeView toView(StreamPushTargetEntity target,
                                         StreamPushRuntimeEntity runtime,
                                         StreamEntity stream) {
        return PushTargetRuntimeView.builder()
                .pushTargetId(target.getId())
                .streamId(target.getStreamId())
                .streamCode(stream == null ? null : stream.getStreamCode())
                .targetCode(target.getTargetCode())
                .targetName(target.getTargetName())
                .pushStatus(runtime == null ? "IDLE" : runtime.getPushStatus())
                .online(runtime == null ? 0 : runtime.getOnline())
                .pusherKey(runtime == null ? null : runtime.getPusherKey())
                .lastError(runtime == null ? null : runtime.getLastError())
                .lastOnlineAt(runtime == null ? null : runtime.getLastOnlineAt())
                .updateTime(runtime == null ? null : runtime.getUpdateTime())
                .build();
    }
}