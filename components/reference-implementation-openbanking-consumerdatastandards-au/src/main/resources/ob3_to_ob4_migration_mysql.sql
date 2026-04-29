-- ============================================================
-- FULL MIGRATION SCRIPT: Accelerator 3 → Accelerator 4
-- Source : openbank_openbankingdb.ob_account_metadata
-- Target : fs_account_metadatadb (all 4 tables)
-- Engine : MySQL / MariaDB
-- ============================================================
-- OB3 PK : (ACCOUNT_ID, USER_ID, METADATA_KEY)
-- METADATA_VALUE holds the value for each key per account/user
-- ============================================================


-- ============================================================
-- SECTION 1: CREATE DATABASE
-- ============================================================
CREATE DATABASE IF NOT EXISTS fs_account_metadatadb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE fs_account_metadatadb;


-- ============================================================
-- SECTION 2: CREATE TABLES
-- ============================================================

-- 2a. Disclosure Options Status
CREATE TABLE IF NOT EXISTS fs_account_doms_status (
    ACCOUNT_ID                  VARCHAR(255)    NOT NULL,
    DISCLOSURE_OPTIONS_STATUS   VARCHAR(255)    NULL,
    LAST_UPDATED_TIMESTAMP      DATETIME        NULL,

    CONSTRAINT pk_fs_account_doms_status
    PRIMARY KEY (ACCOUNT_ID)
);

-- 2b. BNR Permission
CREATE TABLE IF NOT EXISTS fs_account_bnr_permission (
    ACCOUNT_ID              VARCHAR(255)    NOT NULL,
    USER_ID                 VARCHAR(255)    NOT NULL,
    PERMISSION              VARCHAR(255)    NULL,
    LAST_UPDATED_TIMESTAMP  DATETIME        NULL,

    CONSTRAINT pk_fs_account_bnr_permission
    PRIMARY KEY (ACCOUNT_ID, USER_ID)
);

-- 2c. Secondary User Legal Entity
--     Populated exclusively from BLOCKED_LEGAL_ENTITIES CSV split.
--     Each CSV token becomes its own row with LEGAL_ENTITY_STATUS = 'blocked'.
CREATE TABLE IF NOT EXISTS fs_account_secondary_user_legal_entity (
    ACCOUNT_ID              VARCHAR(255)    NOT NULL,
    USER_ID                 VARCHAR(255)    NOT NULL,
    LEGAL_ENTITY_ID         VARCHAR(255)    NOT NULL,
    LEGAL_ENTITY_STATUS     VARCHAR(255)    NULL,
    LAST_UPDATED_TIMESTAMP  DATETIME        NULL,

    CONSTRAINT pk_fs_account_secondary_user_legal_entity
    PRIMARY KEY (ACCOUNT_ID, USER_ID, LEGAL_ENTITY_ID)
);

-- 2d. Secondary User
--     OTHER_ACCOUNTS_AVAILABILITY stored as BOOLEAN (TINYINT(1))
--     OB3 stores it as string 'true' / 'false' → cast on migration
CREATE TABLE IF NOT EXISTS fs_account_secondary_user (
    ACCOUNT_ID                          VARCHAR(255)    NOT NULL,
    USER_ID                             VARCHAR(255)    NOT NULL,
    INSTRUCTION_STATUS                  VARCHAR(255)    NULL,
    OTHER_ACCOUNTS_AVAILABILITY         TINYINT(1)      NULL COMMENT '1 = true, 0 = false',
    LAST_UPDATED_TIMESTAMP              DATETIME        NULL,

    CONSTRAINT pk_fs_account_secondary_user
    PRIMARY KEY (ACCOUNT_ID, USER_ID)
);


-- ============================================================
-- SECTION 3: MIGRATE DATA
-- ============================================================

-- ------------------------------------------------------------
-- 3a. fs_account_doms_status
--     Source key : DISCLOSURE_OPTIONS_STATUS
-- ------------------------------------------------------------
INSERT INTO fs_account_metadatadb.fs_account_doms_status (
    ACCOUNT_ID,
    DISCLOSURE_OPTIONS_STATUS,
    LAST_UPDATED_TIMESTAMP
)
SELECT
    ACCOUNT_ID,
    METADATA_VALUE          AS DISCLOSURE_OPTIONS_STATUS,
    LAST_UPDATED_TIMESTAMP
FROM
    openbank_openbankingdb.ob_account_metadata
WHERE
    METADATA_KEY = 'DISCLOSURE_OPTIONS_STATUS'
    ON DUPLICATE KEY UPDATE
    DISCLOSURE_OPTIONS_STATUS = VALUES(DISCLOSURE_OPTIONS_STATUS),
    LAST_UPDATED_TIMESTAMP    = VALUES(LAST_UPDATED_TIMESTAMP);


-- ------------------------------------------------------------
-- 3b. fs_account_bnr_permission
--     Source key : PERMISSION
-- ------------------------------------------------------------
INSERT INTO fs_account_metadatadb.fs_account_bnr_permission (
    ACCOUNT_ID,
    USER_ID,
    PERMISSION,
    LAST_UPDATED_TIMESTAMP
)
SELECT
    ACCOUNT_ID,
    USER_ID,
    METADATA_VALUE          AS PERMISSION,
    LAST_UPDATED_TIMESTAMP
FROM
    openbank_openbankingdb.ob_account_metadata
WHERE
    METADATA_KEY = 'bnr-permission'
    ON DUPLICATE KEY UPDATE
    PERMISSION             = VALUES(PERMISSION),
    LAST_UPDATED_TIMESTAMP = VALUES(LAST_UPDATED_TIMESTAMP);


-- ------------------------------------------------------------
-- 3c. fs_account_secondary_user_legal_entity
--     Source key : BLOCKED_LEGAL_ENTITIES (CSV of IDs)
--     e.g. METADATA_VALUE = 'ENT001,ENT002,ENT003'
--
--     Each token is split into its own row with
--     LEGAL_ENTITY_STATUS = 'blocked'.
--
--     NOTE : Requires MySQL 8.0+ for recursive CTE support.
--            For MySQL 5.7 / MariaDB see the stored procedure
--            alternative in Section 5 below.
-- ------------------------------------------------------------
INSERT INTO fs_account_metadatadb.fs_account_secondary_user_legal_entity (
    ACCOUNT_ID,
    USER_ID,
    LEGAL_ENTITY_ID,
    LEGAL_ENTITY_STATUS,
    LAST_UPDATED_TIMESTAMP
)
WITH RECURSIVE split AS (
    -- Anchor: extract the first token from the CSV
    SELECT
        ACCOUNT_ID,
        USER_ID,
        LAST_UPDATED_TIMESTAMP,
        TRIM(SUBSTRING_INDEX(METADATA_VALUE, ',', 1))  AS LEGAL_ENTITY_ID,
        IF(
                LOCATE(',', METADATA_VALUE) > 0,
                TRIM(SUBSTRING(METADATA_VALUE, LOCATE(',', METADATA_VALUE) + 1)),
                NULL
        ) AS remainder
    FROM
        openbank_openbankingdb.ob_account_metadata
    WHERE
        METADATA_KEY   = 'BLOCKED_LEGAL_ENTITIES'
      AND METADATA_VALUE IS NOT NULL
      AND METADATA_VALUE <> ''

    UNION ALL

    -- Recursive: peel off one token at a time
    SELECT
        ACCOUNT_ID,
        USER_ID,
        LAST_UPDATED_TIMESTAMP,
        TRIM(SUBSTRING_INDEX(remainder, ',', 1)) AS LEGAL_ENTITY_ID,
        IF(
                LOCATE(',', remainder) > 0,
                TRIM(SUBSTRING(remainder, LOCATE(',', remainder) + 1)),
                NULL
        )  AS remainder
    FROM
        split
    WHERE
        remainder IS NOT NULL
)
SELECT
    ACCOUNT_ID,
    USER_ID,
    LEGAL_ENTITY_ID,
    'blocked' AS LEGAL_ENTITY_STATUS,
    LAST_UPDATED_TIMESTAMP
FROM
    split
WHERE
    LEGAL_ENTITY_ID IS NOT NULL
  AND LEGAL_ENTITY_ID <> ''
    ON DUPLICATE KEY UPDATE
    LEGAL_ENTITY_STATUS    = 'blocked',
    LAST_UPDATED_TIMESTAMP = VALUES(LAST_UPDATED_TIMESTAMP);


-- ------------------------------------------------------------
-- 3d. fs_account_secondary_user
--     Source keys : INSTRUCTION_STATUS
--                   OTHER_ACCOUNTS_AVAILABILITY (string → BOOLEAN)
--
--     Cast logic : 'true'  (case-insensitive) → 1
--                  anything else              → 0
-- ------------------------------------------------------------
INSERT INTO fs_account_metadatadb.fs_account_secondary_user (
    ACCOUNT_ID,
    USER_ID,
    INSTRUCTION_STATUS,
    OTHER_ACCOUNTS_AVAILABILITY,
    LAST_UPDATED_TIMESTAMP
)
SELECT
    ACCOUNT_ID,
    USER_ID,
    MAX(CASE WHEN METADATA_KEY = 'secondaryAccountInstructionStatus'
                 THEN METADATA_VALUE
        END) AS INSTRUCTION_STATUS,
    MAX(CASE WHEN METADATA_KEY = 'otherAccountsAvailability'
                 THEN IF(LOWER(TRIM(METADATA_VALUE)) = 'true', 1, 0)
        END) AS OTHER_ACCOUNTS_AVAILABILITY,
    MAX(LAST_UPDATED_TIMESTAMP) AS LAST_UPDATED_TIMESTAMP
FROM
    openbank_openbankingdb.ob_account_metadata
WHERE
    METADATA_KEY IN ('secondaryAccountInstructionStatus', 'otherAccountsAvailability')
GROUP BY
    ACCOUNT_ID, USER_ID
    ON DUPLICATE KEY UPDATE
    INSTRUCTION_STATUS                = VALUES(INSTRUCTION_STATUS),
    OTHER_ACCOUNTS_AVAILABILITY       = VALUES(OTHER_ACCOUNTS_AVAILABILITY),
    LAST_UPDATED_TIMESTAMP            = VALUES(LAST_UPDATED_TIMESTAMP);


-- ============================================================
-- SECTION 4: VERIFICATION
-- ============================================================
SELECT 'ob_account_metadata → DOMS' AS source, COUNT(*) AS row_count FROM openbank_openbankingdb.ob_account_metadata WHERE METADATA_KEY = 'DISCLOSURE_OPTIONS_STATUS'
UNION ALL
SELECT 'fs_account_doms_status', COUNT(*) FROM fs_account_metadatadb.fs_account_doms_status
UNION ALL
SELECT '---', NULL
UNION ALL
SELECT 'ob_account_metadata → BNR', COUNT(*) FROM openbank_openbankingdb.ob_account_metadata WHERE METADATA_KEY = 'bnr-permission'
UNION ALL
SELECT 'fs_account_bnr_permission', COUNT(*) FROM fs_account_metadatadb.fs_account_bnr_permission
UNION ALL
SELECT '---', NULL
UNION ALL
SELECT 'ob_account_metadata → BLOCKED_LEGAL_ENTITIES (CSV rows)', COUNT(*) FROM openbank_openbankingdb.ob_account_metadata WHERE METADATA_KEY = 'BLOCKED_LEGAL_ENTITIES'
UNION ALL
SELECT 'fs_account_secondary_user_legal_entity (expanded rows)', COUNT(*) FROM fs_account_metadatadb.fs_account_secondary_user_legal_entity
UNION ALL
SELECT '---', NULL
UNION ALL
SELECT 'ob_account_metadata → SECONDARY USER (distinct acct/user)', COUNT(DISTINCT ACCOUNT_ID, USER_ID) FROM openbank_openbankingdb.ob_account_metadata WHERE METADATA_KEY IN ('secondaryAccountInstructionStatus', 'otherAccountsAvailability')
UNION ALL
SELECT 'fs_account_secondary_user', COUNT(*) FROM fs_account_metadatadb.fs_account_secondary_user
UNION ALL
-- Sanity check: no unexpected values in boolean column (should return 0)
SELECT 'fs_account_secondary_user — invalid BOOLEAN values (expect 0)', COUNT(*) FROM fs_account_metadatadb.fs_account_secondary_user WHERE OTHER_ACCOUNTS_AVAILABILITY NOT IN (0, 1) AND OTHER_ACCOUNTS_AVAILABILITY IS NOT NULL;


-- ============================================================
-- SECTION 5: ALTERNATIVE CSV SPLIT — MySQL 5.7 / MariaDB
--            (Use this INSTEAD of the recursive CTE in 3c
--             if your DB does not support recursive CTEs)
-- ============================================================
/*
USE fs_account_metadatadb;

DROP PROCEDURE IF EXISTS migrate_blocked_legal_entities;

DELIMITER $$

CREATE PROCEDURE migrate_blocked_legal_entities()
BEGIN
    DECLARE done       INT DEFAULT 0;
    DECLARE v_account  VARCHAR(255);
    DECLARE v_user     VARCHAR(255);
    DECLARE v_csv      TEXT;
    DECLARE v_ts       DATETIME;
    DECLARE v_count    INT;
    DECLARE i          INT;
    DECLARE v_id       VARCHAR(255);

    DECLARE cur CURSOR FOR
        SELECT ACCOUNT_ID, USER_ID, METADATA_VALUE, LAST_UPDATED_TIMESTAMP
        FROM openbank_openbankingdb.ob_account_metadata
        WHERE METADATA_KEY = 'BLOCKED_LEGAL_ENTITIES'
          AND METADATA_VALUE IS NOT NULL
          AND METADATA_VALUE <> '';

    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

    OPEN cur;

    read_loop: LOOP
        FETCH cur INTO v_account, v_user, v_csv, v_ts;

        IF done THEN
            LEAVE read_loop;
        END IF;

        SET v_count = LENGTH(v_csv) - LENGTH(REPLACE(v_csv, ',', '')) + 1;
        SET i = 1;

        WHILE i <= v_count DO

            SET v_id = TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(v_csv, ',', i), ',', -1));

            IF v_id IS NOT NULL AND v_id <> '' THEN

                INSERT INTO fs_account_metadatadb.fs_account_secondary_user_legal_entity
                    (ACCOUNT_ID, USER_ID, LEGAL_ENTITY_ID, LEGAL_ENTITY_STATUS, LAST_UPDATED_TIMESTAMP)
                VALUES
                    (v_account, v_user, v_id, 'blocked', v_ts)
                ON DUPLICATE KEY UPDATE
                    LEGAL_ENTITY_STATUS    = 'blocked',
                    LAST_UPDATED_TIMESTAMP = v_ts;

            END IF;

            SET i = i + 1;

        END WHILE;

    END LOOP;

    CLOSE cur;

END$$

DELIMITER ;

CALL migrate_blocked_legal_entities();

DROP PROCEDURE IF EXISTS migrate_blocked_legal_entities;
*/
