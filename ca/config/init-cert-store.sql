CREATE TABLE certificates (
  serial_number            bytea NOT NULL,
  authority_key_identifier bytea NOT NULL,
  ca_label                 bytea,
  status                   bytea NOT NULL,
  reason                   int,
  expiry                   timestamptz,
  revoked_at               timestamptz,
  pem                      bytea NOT NULL,
  PRIMARY KEY(serial_number, authority_key_identifier)
);

CREATE TABLE ocsp_responses (
  serial_number            bytea NOT NULL,
  authority_key_identifier bytea NOT NULL,
  body                     bytea NOT NULL,
  expiry                   timestamptz,
  PRIMARY KEY(serial_number, authority_key_identifier),
  FOREIGN KEY(serial_number, authority_key_identifier) REFERENCES certificates(serial_number, authority_key_identifier)
);

ALTER TABLE certificates
    ADD COLUMN issued_at timestamp DEFAULT '0000-00-00 00:00:00',
    ADD COLUMN not_before timestamp DEFAULT '0000-00-00 00:00:00',
    ADD COLUMN metadata JSON,
    ADD COLUMN sans JSON,
    ADD COLUMN common_name TEXT;
