-- MySQL schema for API Monitoring Dashboard
-- Hibernate ddl-auto=update will create/update this automatically.

CREATE DATABASE IF NOT EXISTS api_monitoring;
USE api_monitoring;

CREATE TABLE IF NOT EXISTS api_logs (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    api_name            VARCHAR(255) NOT NULL,
    work_item_id        VARCHAR(100),
    request_timestamp   DATETIME(6) NOT NULL,
    response_timestamp  DATETIME(6) NOT NULL,
    response_code       INT,
    status              VARCHAR(20) NOT NULL,
    error_message       VARCHAR(2000),
    response_time       BIGINT NOT NULL
);

CREATE INDEX idx_api_name ON api_logs (api_name);
CREATE INDEX idx_work_item_id ON api_logs (work_item_id);
CREATE INDEX idx_status ON api_logs (status);
CREATE INDEX idx_request_timestamp ON api_logs (request_timestamp);
