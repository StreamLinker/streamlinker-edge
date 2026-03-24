package io.streamlinker.edge.infra.db.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class EntityShapeTest {

    @Test
    void shouldExposeApprovedPersistenceFields() throws Exception {
        assertEntity(StreamEntity.class, "stream", "id", new FieldSpec[] {
                spec("id", Long.class),
                spec("streamCode", String.class),
                spec("name", String.class),
                spec("sourceUrl", String.class),
                spec("sourceProtocol", String.class),
                spec("accessMode", String.class),
                spec("localApp", String.class),
                spec("localStream", String.class),
                spec("enabled", Integer.class),
                spec("expectedState", String.class),
                spec("deleted", Integer.class),
                spec("remark", String.class),
                spec("createTime", LocalDateTime.class),
                spec("updateTime", LocalDateTime.class)
        });

        assertEntity(StreamPushTargetEntity.class, "stream_push_target", "id", new FieldSpec[] {
                spec("id", Long.class),
                spec("streamId", Long.class),
                spec("targetCode", String.class),
                spec("targetName", String.class),
                spec("targetType", String.class),
                spec("targetProtocol", String.class),
                spec("targetUrl", String.class),
                spec("targetApp", String.class),
                spec("targetStream", String.class),
                spec("enabled", Integer.class),
                spec("expectedState", String.class),
                spec("deleted", Integer.class),
                spec("remark", String.class),
                spec("createTime", LocalDateTime.class),
                spec("updateTime", LocalDateTime.class)
        });

        assertEntity(StreamRuntimeEntity.class, "stream_runtime", "streamId", new FieldSpec[] {
                spec("streamId", Long.class),
                spec("pullStatus", String.class),
                spec("mediaKey", String.class),
                spec("zlmTaskKey", String.class),
                spec("localOnline", Integer.class),
                spec("lastOnlineAt", LocalDateTime.class),
                spec("lastError", String.class),
                spec("reconcileVersion", Long.class),
                spec("updateTime", LocalDateTime.class)
        });

        assertEntity(StreamPushRuntimeEntity.class, "stream_push_runtime", "pushTargetId", new FieldSpec[] {
                spec("pushTargetId", Long.class),
                spec("pushStatus", String.class),
                spec("pusherKey", String.class),
                spec("online", Integer.class),
                spec("lastOnlineAt", LocalDateTime.class),
                spec("lastError", String.class),
                spec("reconcileVersion", Long.class),
                spec("updateTime", LocalDateTime.class)
        });

        assertEntity(StreamProcessEntity.class, "stream_process", "id", new FieldSpec[] {
                spec("id", Long.class),
                spec("processType", String.class),
                spec("streamId", Long.class),
                spec("pushTargetId", Long.class),
                spec("step", Integer.class),
                spec("status", String.class),
                spec("retryCount", Integer.class),
                spec("maxRetryCount", Integer.class),
                spec("requestSnapshot", String.class),
                spec("contextSnapshot", String.class),
                spec("errorMessage", String.class),
                spec("startTime", LocalDateTime.class),
                spec("finishTime", LocalDateTime.class),
                spec("createTime", LocalDateTime.class),
                spec("updateTime", LocalDateTime.class)
        });
    }

    private static void assertEntity(Class<?> type, String tableName, String idFieldName, FieldSpec[] specs) throws Exception {
        TableName annotation = type.getAnnotation(TableName.class);
        assertThat(annotation).isNotNull();
        assertThat(annotation.value()).isEqualTo(tableName);
        assertThat(type.getDeclaredField(idFieldName).getAnnotation(TableId.class)).isNotNull();
        for (FieldSpec spec : specs) {
            Field field = type.getDeclaredField(spec.name());
            assertThat(field.getType()).isEqualTo(spec.type());
        }
    }

    private static FieldSpec spec(String name, Class<?> type) {
        return new FieldSpec(name, type);
    }

    private record FieldSpec(String name, Class<?> type) {
    }
}