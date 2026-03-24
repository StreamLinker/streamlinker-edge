package io.streamlinker.edge.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DomainEnumContractTest {

    @Test
    void shouldExposeEnterpriseEnumValues() {
        assertThat(AccessMode.values()).containsExactly(AccessMode.FFMPEG, AccessMode.PROXY);
        assertThat(ExpectedState.values()).containsExactly(ExpectedState.RUNNING, ExpectedState.STOPPED);
        assertThat(PullStatus.values()).containsExactly(PullStatus.IDLE, PullStatus.STARTING, PullStatus.RUNNING, PullStatus.STOPPING, PullStatus.FAILED);
        assertThat(PushStatus.values()).containsExactly(PushStatus.IDLE, PushStatus.STARTING, PushStatus.RUNNING, PushStatus.STOPPING, PushStatus.FAILED);
        assertThat(ProcessStatus.values()).containsExactly(ProcessStatus.INIT, ProcessStatus.RUNNING, ProcessStatus.SUCCESS, ProcessStatus.FAILED, ProcessStatus.CANCELED);
        assertThat(ProcessType.values()).containsExactly(ProcessType.PULL_UP, ProcessType.PULL_DOWN, ProcessType.PUSH_UP, ProcessType.PUSH_DOWN);
        assertThat(StreamState.values()).containsExactly(StreamState.IDLE, StreamState.STARTING, StreamState.RUNNING, StreamState.STOPPING, StreamState.FAILED);
    }
}
