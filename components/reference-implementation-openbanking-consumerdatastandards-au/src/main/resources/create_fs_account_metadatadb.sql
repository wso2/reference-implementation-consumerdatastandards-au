-- ============================================================
-- SCHEMA-ONLY SETUP: Account Metadata Database
-- Creates the database and the four tables required by the webapp
-- ============================================================

CREATE DATABASE IF NOT EXISTS fs_account_metadatadb
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE fs_account_metadatadb;

CREATE TABLE IF NOT EXISTS fs_account_doms_status (
    ACCOUNT_ID                 VARCHAR(255)    NOT NULL,
    DISCLOSURE_OPTIONS_STATUS   VARCHAR(255)    NULL,
    LAST_UPDATED_TIMESTAMP      DATETIME        NULL,

    CONSTRAINT pk_fs_account_doms_status
    PRIMARY KEY (ACCOUNT_ID)
);

CREATE TABLE IF NOT EXISTS fs_account_bnr_permission (
    ACCOUNT_ID              VARCHAR(255)    NOT NULL,
    USER_ID                 VARCHAR(255)    NOT NULL,
    PERMISSION              VARCHAR(255)    NULL,
    LAST_UPDATED_TIMESTAMP  DATETIME        NULL,

    CONSTRAINT pk_fs_account_bnr_permission
    PRIMARY KEY (ACCOUNT_ID, USER_ID)
);

CREATE TABLE IF NOT EXISTS fs_account_secondary_user_legal_entity (
    ACCOUNT_ID              VARCHAR(255)    NOT NULL,
    USER_ID                 VARCHAR(255)    NOT NULL,
    LEGAL_ENTITY_ID         VARCHAR(255)    NOT NULL,
    LEGAL_ENTITY_STATUS     VARCHAR(255)    NULL,
    LAST_UPDATED_TIMESTAMP  DATETIME        NULL,

    CONSTRAINT pk_fs_account_secondary_user_legal_entity
    PRIMARY KEY (ACCOUNT_ID, USER_ID, LEGAL_ENTITY_ID)
);

CREATE TABLE IF NOT EXISTS fs_account_secondary_user (
    ACCOUNT_ID                      VARCHAR(255)    NOT NULL,
    USER_ID                         VARCHAR(255)    NOT NULL,
    INSTRUCTION_STATUS              VARCHAR(255)    NULL,
    OTHER_ACCOUNTS_AVAILABILITY     TINYINT(1)      NULL COMMENT '1 = true, 0 = false',
    LAST_UPDATED_TIMESTAMP          DATETIME        NULL,

    CONSTRAINT pk_fs_account_secondary_user
    PRIMARY KEY (ACCOUNT_ID, USER_ID)
);