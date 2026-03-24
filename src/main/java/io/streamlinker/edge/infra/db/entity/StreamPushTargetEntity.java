package io.streamlinker.edge.infra.db.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("stream_push_target")
public class StreamPushTargetEntity {

    @TableId
    private Long id;
    private Long streamId;
    private String targetCode;
    private String targetName;
    private String targetType;
    private String targetProtocol;
    private String targetUrl;
    private String targetApp;
    private String targetStream;
    private Integer enabled;
    private String expectedState;
    private Integer deleted;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}