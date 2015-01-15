DROP TABLE IF EXISTS auditrepo.advanced_audit;
CREATE TABLE auditrepo.advanced_audit
(
    audit_id BIGINT NOT NULL,
    service_name VARCHAR(64),
    subsystem VARCHAR(16),
    message_direction VARCHAR(8),
    message_id VARCHAR(96),
    user_name VARCHAR(64),
    user_roles VARCHAR(64),
    source_system VARCHAR(64),
    source_community VARCHAR(64),
    PRIMARY KEY (audit_id)
);
