-- SpeakOut — CSC584 Group 3
-- 7-table schema per FABLE_BUILD_PROMPT.md §6.
-- ENUM columns from the spec are VARCHAR + CHECK constraints (simpler with JDBC).
-- Re-runnable: drops everything first.

DROP TABLE IF EXISTS case_handlers CASCADE;
DROP TABLE IF EXISTS evidence   CASCADE;
DROP TABLE IF EXISTS case_notes CASCADE;
DROP TABLE IF EXISTS cases      CASCADE;
DROP TABLE IF EXISTS reports    CASCADE;
DROP TABLE IF EXISTS category   CASCADE;
DROP TABLE IF EXISTS users      CASCADE;
DROP TABLE IF EXISTS school     CASCADE;

CREATE TABLE school (
    school_id   VARCHAR(50)  PRIMARY KEY,
    code        VARCHAR(20)  UNIQUE NOT NULL,      -- e.g. "BEA8501", validated at registration
    name        VARCHAR(150) NOT NULL,
    address     VARCHAR(255)
);

CREATE TABLE users (
    user_id       VARCHAR(50)  PRIMARY KEY,
    school_id     VARCHAR(50)  NOT NULL REFERENCES school(school_id),
    full_name     VARCHAR(100) NOT NULL,
    email         VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,           -- bcrypt
    role          VARCHAR(20)  NOT NULL CHECK (role IN ('Student', 'Teacher', 'Admin', 'PIBG', 'PDRM')),
    class_form    VARCHAR(50),                     -- students only, e.g. "Form 4 Bestari"
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE category (
    category_id VARCHAR(50)  PRIMARY KEY,
    name        VARCHAR(50)  NOT NULL,             -- Verbal, Cyber, Physical, Social
    description VARCHAR(255)
);

CREATE TABLE reports (
    report_id      VARCHAR(50)  PRIMARY KEY,       -- displayed as Case ID, e.g. #SO-0421
    school_id      VARCHAR(50)  NOT NULL REFERENCES school(school_id),
    reporter_id    VARCHAR(50)  NOT NULL REFERENCES users(user_id),
    category_id    VARCHAR(50)  NOT NULL REFERENCES category(category_id),
    title          VARCHAR(100) NOT NULL,
    description    TEXT         NOT NULL,
    location       VARCHAR(100),
    incident_date  DATE,
    severity       VARCHAR(20)  NOT NULL CHECK (severity IN ('Low', 'Medium', 'High', 'Critical')),
    anonymity_flag BOOLEAN      NOT NULL DEFAULT TRUE,
    status         VARCHAR(20)  NOT NULL CHECK (status IN ('Draft', 'Submitted', 'In review', 'Resolved')),
    submitted_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE cases (
    case_id     VARCHAR(50) PRIMARY KEY,
    report_id   VARCHAR(50) NOT NULL UNIQUE REFERENCES reports(report_id) ON DELETE CASCADE,
    assigned_to VARCHAR(50) REFERENCES users(user_id),   -- lead counsellor/teacher; NULL until assigned
    status      VARCHAR(30) NOT NULL CHECK (status IN ('New', 'Under Investigation', 'Resolved', 'Closed')),
    priority    VARCHAR(20) NOT NULL CHECK (priority IN ('Low', 'Medium', 'High', 'Critical')),
    created_at  TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE TABLE case_notes (
    note_id    VARCHAR(50) PRIMARY KEY,
    case_id    VARCHAR(50) NOT NULL REFERENCES cases(case_id) ON DELETE CASCADE,
    author_id  VARCHAR(50) NOT NULL REFERENCES users(user_id),
    body       TEXT        NOT NULL,
    visibility VARCHAR(20) NOT NULL CHECK (visibility IN ('internal', 'reporter-visible')),
    created_at TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE TABLE evidence (
    evidence_id VARCHAR(50)  PRIMARY KEY,
    report_id   VARCHAR(50)  NOT NULL REFERENCES reports(report_id) ON DELETE CASCADE,
    file_url    VARCHAR(500) NOT NULL,
    file_type   VARCHAR(20)  NOT NULL CHECK (file_type IN ('image', 'video', 'document')),
    file_size   INT          NOT NULL CHECK (file_size > 0 AND file_size <= 52428800),  -- max 50MB
    uploaded_at TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- Dashboard / tenant-boundary query paths
CREATE INDEX idx_users_school      ON users(school_id);
CREATE INDEX idx_reports_school    ON reports(school_id);
CREATE INDEX idx_reports_reporter  ON reports(reporter_id);
CREATE INDEX idx_reports_status    ON reports(status);
CREATE INDEX idx_cases_assigned    ON cases(assigned_to);
CREATE INDEX idx_case_notes_case   ON case_notes(case_id);
CREATE INDEX idx_evidence_report   ON evidence(report_id);

-- Co-handlers on a case (screen 9: co-handler list + "add co-handler")
CREATE TABLE case_handlers (
    case_id  VARCHAR(50) NOT NULL REFERENCES cases(case_id) ON DELETE CASCADE,
    user_id  VARCHAR(50) NOT NULL REFERENCES users(user_id),
    added_at TIMESTAMP   NOT NULL DEFAULT NOW(),
    PRIMARY KEY (case_id, user_id)
);

-- App-generated display IDs (SO-0413, CS-0409, ...); seed.sql restarts these
DROP SEQUENCE IF EXISTS report_seq;   CREATE SEQUENCE report_seq   START 413;
DROP SEQUENCE IF EXISTS case_seq;     CREATE SEQUENCE case_seq     START 409;
DROP SEQUENCE IF EXISTS note_seq;     CREATE SEQUENCE note_seq     START 9;
DROP SEQUENCE IF EXISTS evidence_seq; CREATE SEQUENCE evidence_seq START 5;
DROP SEQUENCE IF EXISTS user_seq;     CREATE SEQUENCE user_seq     START 1001;
