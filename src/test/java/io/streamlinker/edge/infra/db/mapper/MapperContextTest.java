package io.streamlinker.edge.infra.db.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:mapper_context;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.sql.init.mode=always",
        "spring.sql.init.schema-locations=classpath:sql/streamlinker-edge-schema-h2.sql"
})
class MapperContextTest {

    @Autowired
    private StreamMapper streamMapper;

    @Autowired
    private StreamPushTargetMapper streamPushTargetMapper;

    @Autowired
    private StreamRuntimeMapper streamRuntimeMapper;

    @Autowired
    private StreamPushRuntimeMapper streamPushRuntimeMapper;

    @Autowired
    private StreamProcessMapper streamProcessMapper;

    @Test
    void shouldCreateAllMapperBeans() {
        assertThat(streamMapper).isNotNull();
        assertThat(streamPushTargetMapper).isNotNull();
        assertThat(streamRuntimeMapper).isNotNull();
        assertThat(streamPushRuntimeMapper).isNotNull();
        assertThat(streamProcessMapper).isNotNull();
    }
}