CREATE TABLE visitor_profile (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    visitor_code VARCHAR(64) NOT NULL UNIQUE,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
);

CREATE TABLE chat_session (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    visitor_id BIGINT NOT NULL,
    companion_style VARCHAR(32) NOT NULL,
    title VARCHAR(120),
    latest_emotion_tag VARCHAR(32),
    latest_risk_level VARCHAR(32) NOT NULL DEFAULT 'NONE',
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    INDEX idx_chat_session_visitor (visitor_id, created_at)
);

CREATE TABLE doodle_asset (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    visitor_id BIGINT NOT NULL,
    storage_path VARCHAR(500) NOT NULL,
    public_url VARCHAR(500) NOT NULL,
    original_filename VARCHAR(255),
    content_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    width INT,
    height INT,
    status VARCHAR(32) NOT NULL DEFAULT 'UPLOADED',
    bound_message_id BIGINT,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    INDEX idx_doodle_asset_visitor (visitor_id, created_at),
    INDEX idx_doodle_asset_status (status, created_at)
);

CREATE TABLE chat_message (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_id BIGINT NOT NULL,
    visitor_id BIGINT NOT NULL,
    role VARCHAR(32) NOT NULL,
    input_type VARCHAR(32) NOT NULL,
    content TEXT,
    short_note VARCHAR(500),
    emotion_tag VARCHAR(32),
    submitted_emotion_tag VARCHAR(32),
    doodle_asset_id BIGINT,
    moderation_status VARCHAR(32) NOT NULL DEFAULT 'PASS',
    risk_level VARCHAR(32) NOT NULL DEFAULT 'NONE',
    provider_type VARCHAR(32) NOT NULL DEFAULT 'LOCAL',
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    INDEX idx_chat_message_session (session_id, created_at),
    INDEX idx_chat_message_visitor (visitor_id, created_at),
    INDEX idx_chat_message_emotion (visitor_id, emotion_tag, created_at)
);

CREATE TABLE content_rule (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    keyword VARCHAR(120) NOT NULL,
    category VARCHAR(32) NOT NULL,
    action VARCHAR(32) NOT NULL,
    enabled TINYINT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    UNIQUE KEY uk_content_rule_keyword (keyword)
);

CREATE TABLE risk_rule (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    keyword VARCHAR(120) NOT NULL,
    risk_level VARCHAR(32) NOT NULL,
    enabled TINYINT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    UNIQUE KEY uk_risk_rule_keyword (keyword)
);

CREATE TABLE risk_event (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    visitor_id BIGINT NOT NULL,
    session_id BIGINT NOT NULL,
    message_id BIGINT NOT NULL,
    risk_level VARCHAR(32) NOT NULL,
    matched_keyword VARCHAR(120),
    status VARCHAR(32) NOT NULL DEFAULT 'OPEN',
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    INDEX idx_risk_event_status (status, created_at)
);

CREATE TABLE support_resource (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(120) NOT NULL,
    contact VARCHAR(120) NOT NULL,
    description VARCHAR(500),
    enabled TINYINT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
);

CREATE TABLE admin_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(64) NOT NULL UNIQUE,
    password_hash VARCHAR(128) NOT NULL,
    display_name VARCHAR(80) NOT NULL,
    enabled TINYINT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
);
