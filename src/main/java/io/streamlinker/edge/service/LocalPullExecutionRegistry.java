package io.streamlinker.edge.service;

import io.streamlinker.edge.domain.AccessMode;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LocalPullExecutionRegistry {

    private final Map<String, PullExecutionState> states = new ConcurrentHashMap<>();

    public void markRunning(String streamId, AccessMode accessMode, String taskKey) {
        states.put(streamId, new PullExecutionState(accessMode, taskKey, true));
    }

    public void markStopped(String streamId) {
        states.remove(streamId);
    }

    public boolean isOnline(String streamId) {
        PullExecutionState state = states.get(streamId);
        return state != null && state.online();
    }

    public PullExecutionState get(String streamId) {
        return states.get(streamId);
    }

    public void clear() {
        states.clear();
    }

    public record PullExecutionState(AccessMode accessMode, String taskKey, boolean online) {
    }
}