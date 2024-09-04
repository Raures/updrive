DROP TABLE IF EXISTS file_metadata;
CREATE TABLE file_metadata
    (id SERIAL PRIMARY KEY,
    slot_id VARCHAR(36) UNIQUE,
    available_hours NUMERIC(4),
    active BOOLEAN NOT NULL,
    expired BOOLEAN DEFAULT FALSE,
    expiration_date TIMESTAMP DEFAULT NULL,
    creation_date TIMESTAMP);