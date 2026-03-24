package io.streamlinker.edge.service;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LocalPushExecutionRegistry {

    private final Map<String, PushExecutionState> states = new ConcurrentHashMap<>();

    public void markRunning(String targetCode, String pusherKey) {
        states.put(targetCode, new PushExecutionState(pusherKey, true));
    }

    public void markStopped(String targetCode) {
        states.remove(targetCode);
    }

    public boolean isOnline(String targetCode) {
        PushExecutionState state = states.get(targetCode);
        return state != null && state.online();
    }

    public PushExecutionState get(String targetCode) {
        return states.get(targetCode);
    }

    public void clear() {
        states.clear();
    }

    public record PushExecutionState(String pusherKey, boolean online) {
    }
}