package io.streamlinker.edge;

import io.streamlinker.edge.service.PullExecutionGateway;
import io.streamlinker.edge.service.PushExecutionGateway;
import io.streamlinker.zlm.api.ZlmClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:edge_app;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.sql.init.mode=always",
        "spring.sql.init.schema-locations=classpath:sql/streamlinker-edge-schema-h2.sql"
})
class StreamlinkerEdgeApplicationTest {

    @Autowired
    private ZlmClient zlmClient;

    @Autowired
    private PullExecutionGateway pullExecutionGateway;

    @Autowired
    private PushExecutionGateway pushExecutionGateway;

    @Autowired
    private Environment environment;

    @Test
    void contextLoads() {
        assertThat(zlmClient).isNotNull();
        assertThat(pullExecutionGateway).isNotNull();
        assertThat(pushExecutionGateway).isNotNull();
        assertThat(environment.getProperty("spring.datasource.url")).contains("jdbc:h2:mem:edge_app");
        assertThat(environment.getProperty("spring.datasource.driver-class-name")).isEqualTo("org.h2.Driver");
        assertThat(environment.getProperty("streamlinker.edge.media.simulation-enabled")).isEqualTo("true");
    }
}