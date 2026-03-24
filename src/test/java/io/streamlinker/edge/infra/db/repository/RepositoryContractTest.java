package io.streamlinker.edge.infra.db.repository;

import io.streamlinker.edge.infra.db.entity.StreamProcessEntity;
import io.streamlinker.edge.infra.db.entity.StreamPushRuntimeEntity;
import io.streamlinker.edge.infra.db.entity.StreamRuntimeEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:repo_contract;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.sql.init.mode=always",
        "spring.sql.init.schema-locations=classpath:sql/streamlinker-edge-schema-h2.sql"
})
class RepositoryContractTest {

    @Autowired
    private StreamRepository streamRepository;

    @Autowired
    private StreamPushTargetRepository streamPushTargetRepository;

    @Autowired
    private StreamRuntimeRepository streamRuntimeRepository;

    @Autowired
    private StreamPushRuntimeRepository streamPushRuntimeRepository;

    @Autowired
    private StreamProcessRepository streamProcessRepository;

    @Test
    void shouldExposeRepositoryContractsNeededByDesign() {
        assertThat(streamRepository).isNotNull();
        assertThat(streamPushTargetRepository).isNotNull();
        assertThat(streamRuntimeRepository).isNotNull();
        assertThat(streamPushRuntimeRepository).isNotNull();
        assertThat(streamProcessRepository).isNotNull();
        assertHasMethod(StreamRepository.class, "findByStreamId", String.class);
        assertHasMethod(StreamRepository.class, "findActiveByExpectedState", String.class);
        assertHasMethod(StreamPushTargetRepository.class, "findEnabledByStreamId", Long.class);
        assertHasMethod(StreamPushTargetRepository.class, "findByPushTargetId", Long.class);
        assertHasMethod(StreamRuntimeRepository.class, "findByStreamId", Long.class);
        assertHasMethod(StreamRuntimeRepository.class, "saveOrUpdate", StreamRuntimeEntity.class);
        assertHasMethod(StreamProcessRepository.class, "findActiveByStreamIdAndType", Long.class, String.class);
        assertHasMethod(StreamProcessRepository.class, "findActiveByPushTargetIdAndType", Long.class, String.class);
        assertHasMethod(StreamProcessRepository.class, "create", StreamProcessEntity.class);
        assertHasMethod(StreamPushRuntimeRepository.class, "findByPushTargetId", Long.class);
        assertHasMethod(StreamPushRuntimeRepository.class, "saveOrUpdate", StreamPushRuntimeEntity.class);
    }

    private static void assertHasMethod(Class<?> type, String name, Class<?>... parameterTypes) {
        Method method = org.springframework.util.ReflectionUtils.findMethod(type, name, parameterTypes);
        assertThat(method)
                .withFailMessage("Expected method %s on %s", name, type.getName())
                .isNotNull();
    }
}