CREATE TABLE IF NOT EXISTS `stream` (
  `id` BIGINT NOT NULL,
  `stream_code` VARCHAR(64) NOT NULL,
  `name` VARCHAR(128) NOT NULL,
  `source_url` VARCHAR(1024) NOT NULL,
  `source_protocol` VARCHAR(32) NOT NULL,
  `access_mode` VARCHAR(32) NOT NULL,
  `local_app` VARCHAR(64) NOT NULL,
  `local_stream` VARCHAR(128) NOT NULL,
  `enabled` TINYINT NOT NULL DEFAULT 1,
  `expected_state` VARCHAR(16) NOT NULL DEFAULT 'STOPPED',
  `deleted` TINYINT NOT NULL DEFAULT 0,
  `remark` VARCHAR(512) NULL,
  `create_time` DATETIME NOT NULL,
  `update_time` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_stream_code` (`stream_code`),
  UNIQUE KEY `uk_local_app_stream` (`local_app`, `local_stream`),
  KEY `idx_stream_enabled_expected` (`enabled`, `expected_state`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `stream_push_target` (
  `id` BIGINT NOT NULL,
  `stream_id` BIGINT NOT NULL,
  `target_code` VARCHAR(64) NOT NULL,
  `target_name` VARCHAR(128) NOT NULL,
  `target_type` VARCHAR(32) NOT NULL,
  `target_protocol` VARCHAR(32) NOT NULL,
  `target_url` VARCHAR(1024) NOT NULL,
  `target_app` VARCHAR(64) NULL,
  `target_stream` VARCHAR(128) NULL,
  `enabled` TINYINT NOT NULL DEFAULT 1,
  `expected_state` VARCHAR(16) NOT NULL DEFAULT 'STOPPED',
  `deleted` TINYINT NOT NULL DEFAULT 0,
  `remark` VARCHAR(512) NULL,
  `create_time` DATETIME NOT NULL,
  `update_time` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_push_target_code` (`target_code`),
  UNIQUE KEY `uk_stream_target_url` (`stream_id`, `target_url`(255)),
  KEY `idx_push_target_stream` (`stream_id`),
  KEY `idx_push_target_enabled_expected` (`enabled`, `expected_state`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `stream_runtime` (
  `stream_id` BIGINT NOT NULL,
  `pull_status` VARCHAR(16) NOT NULL DEFAULT 'IDLE',
  `media_key` VARCHAR(256) NULL,
  `zlm_task_key` VARCHAR(256) NULL,
  `local_online` TINYINT NOT NULL DEFAULT 0,
  `last_online_at` DATETIME NULL,
  `last_error` VARCHAR(1024) NULL,
  `reconcile_version` BIGINT NOT NULL DEFAULT 0,
  `update_time` DATETIME NOT NULL,
  PRIMARY KEY (`stream_id`),
  KEY `idx_stream_runtime_status_online` (`pull_status`, `local_online`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `stream_push_runtime` (
  `push_target_id` BIGINT NOT NULL,
  `push_status` VARCHAR(16) NOT NULL DEFAULT 'IDLE',
  `pusher_key` VARCHAR(256) NULL,
  `online` TINYINT NOT NULL DEFAULT 0,
  `last_online_at` DATETIME NULL,
  `last_error` VARCHAR(1024) NULL,
  `reconcile_version` BIGINT NOT NULL DEFAULT 0,
  `update_time` DATETIME NOT NULL,
  PRIMARY KEY (`push_target_id`),
  KEY `idx_push_runtime_status_online` (`push_status`, `online`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `stream_process` (
  `id` BIGINT NOT NULL,
  `process_type` VARCHAR(16) NOT NULL,
  `stream_id` BIGINT NULL,
  `push_target_id` BIGINT NULL,
  `step` INT NOT NULL DEFAULT 0,
  `status` VARCHAR(16) NOT NULL DEFAULT 'INIT',
  `retry_count` INT NOT NULL DEFAULT 0,
  `max_retry_count` INT NOT NULL DEFAULT 10,
  `request_snapshot` JSON NULL,
  `context_snapshot` JSON NULL,
  `error_message` VARCHAR(2048) NULL,
  `start_time` DATETIME NULL,
  `finish_time` DATETIME NULL,
  `create_time` DATETIME NOT NULL,
  `update_time` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_process_status_type` (`status`, `process_type`),
  KEY `idx_process_stream_type_status` (`stream_id`, `process_type`, `status`),
  KEY `idx_process_target_type_status` (`push_target_id`, `process_type`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
