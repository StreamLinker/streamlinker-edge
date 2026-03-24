package io.streamlinker.edge.service.process;

import io.streamlinker.edge.domain.ProcessStatus;
import io.streamlinker.edge.domain.ProcessType;
import io.streamlinker.edge.infra.db.entity.StreamProcessEntity;
import io.streamlinker.edge.infra.db.repository.StreamProcessRepository;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StreamProcessCommandServiceTest {

    @Test
    void shouldDeduplicateActivePullProcessAndInitializeMetadata() {
        StreamProcessRepository repository = mock(StreamProcessRepository.class);
        StreamProcessCommandService service = new StreamProcessCommandService(repository);

        StreamProcessEntity active = new StreamProcessEntity();
        active.setId(1L);
        when(repository.findActiveByStreamIdAndType(10L, ProcessType.PULL_UP.name())).thenReturn(active);

        StreamProcessEntity result = service.createPullProcess(2L, 10L, ProcessType.PULL_UP);
        assertThat(result).isSameAs(active);

        when(repository.findActiveByStreamIdAndType(11L, ProcessType.PULL_UP.name())).thenReturn(null);
        when(repository.create(any(StreamProcessEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StreamProcessEntity created = service.createPullProcess(3L, 11L, ProcessType.PULL_UP);
        assertThat(created.getStatus()).isEqualTo(ProcessStatus.INIT.name());
        assertThat(created.getStep()).isEqualTo(0);
        assertThat(created.getMaxRetryCount()).isEqualTo(10);
    }

    @Test
    void shouldDeduplicateActivePushProcessAndInitializeMetadata() {
        StreamProcessRepository repository = mock(StreamProcessRepository.class);
        StreamProcessCommandService service = new StreamProcessCommandService(repository);

        StreamProcessEntity active = new StreamProcessEntity();
        active.setId(4L);
        when(repository.findActiveByPushTargetIdAndType(20L, ProcessType.PUSH_UP.name())).thenReturn(active);

        StreamProcessEntity result = service.createPushProcess(5L, 20L, ProcessType.PUSH_UP);
        assertThat(result).isSameAs(active);

        when(repository.findActiveByPushTargetIdAndType(21L, ProcessType.PUSH_UP.name())).thenReturn(null);
        when(repository.create(any(StreamProcessEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StreamProcessEntity created = service.createPushProcess(6L, 21L, ProcessType.PUSH_UP);
        assertThat(created.getStatus()).isEqualTo(ProcessStatus.INIT.name());
        assertThat(created.getStep()).isEqualTo(0);
        assertThat(created.getMaxRetryCount()).isEqualTo(10);
    }

    @Test
    void shouldAdvanceLifecycleForExistingProcess() {
        StreamProcessRepository repository = mock(StreamProcessRepository.class);
        StreamProcessCommandService service = new StreamProcessCommandService(repository);

        StreamProcessEntity entity = new StreamProcessEntity();
        entity.setId(7L);
        entity.setStatus(ProcessStatus.INIT.name());
        entity.setStep(0);
        entity.setRetryCount(0);
        when(repository.findById(7L)).thenReturn(entity);
        when(repository.save(any(StreamProcessEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StreamProcessEntity running = service.markRunning(7L);
        assertThat(running.getStatus()).isEqualTo(ProcessStatus.RUNNING.name());
        assertThat(running.getStartTime()).isNotNull();

        StreamProcessEntity stepped = service.advanceStep(7L, 2, "{\"phase\":\"pull-online\"}");
        assertThat(stepped.getStep()).isEqualTo(2);
        assertThat(stepped.getContextSnapshot()).contains("pull-online");

        StreamProcessEntity retried = service.incrementRetryCount(7L, "temporary failure");
        assertThat(retried.getRetryCount()).isEqualTo(1);
        assertThat(retried.getErrorMessage()).isEqualTo("temporary failure");

        StreamProcessEntity failed = service.markFailed(7L, 2, "permanent failure");
        assertThat(failed.getStatus()).isEqualTo(ProcessStatus.FAILED.name());
        assertThat(failed.getFinishTime()).isNotNull();

        StreamProcessEntity success = service.markSuccess(7L, 3, "{\"phase\":\"done\"}");
        assertThat(success.getStatus()).isEqualTo(ProcessStatus.SUCCESS.name());
        assertThat(success.getStep()).isEqualTo(3);
        assertThat(success.getContextSnapshot()).contains("done");
        assertThat(success.getErrorMessage()).isNull();
    }

    @Test
    void shouldRejectUnknownProcessUpdates() {
        StreamProcessRepository repository = mock(StreamProcessRepository.class);
        StreamProcessCommandService service = new StreamProcessCommandService(repository);
        when(repository.findById(99L)).thenReturn(null);

        assertThatThrownBy(() -> service.markRunning(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown processId: 99");
    }
}