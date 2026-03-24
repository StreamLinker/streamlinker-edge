package io.streamlinker.edge;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@MapperScan("io.streamlinker.edge.infra.db.mapper")
public class StreamlinkerEdgeApplication {

    public static void main(String[] args) {
        SpringApplication.run(StreamlinkerEdgeApplication.class, args);
    }
}