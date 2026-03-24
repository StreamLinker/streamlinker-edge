package io.streamlinker.edge.service;

import io.streamlinker.edge.infra.db.entity.StreamEntity;
import io.streamlinker.edge.infra.db.entity.StreamPushRuntimeEntity;
import io.streamlinker.edge.infra.db.entity.StreamPushTargetEntity;

public interface PushExecutionGateway {

    String startPush(StreamEntity stream, StreamPushTargetEntity target);

    void stopPush(StreamPushTargetEntity target, StreamPushRuntimeEntity runtime);

    boolean isOnline(StreamPushTargetEntity target);
}