-- Reference schema. Spring Data JPA creates/updates these tables automatically.
CREATE TABLE monitors (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(120) NOT NULL,
  url VARCHAR(1000) NOT NULL,
  expected_status INTEGER NOT NULL,
  timeout_ms INTEGER NOT NULL,
  retry_count INTEGER NOT NULL,
  check_interval_seconds INTEGER NOT NULL,
  active BOOLEAN NOT NULL,
  current_status VARCHAR(20) NOT NULL,
  last_checked_at TIMESTAMPTZ,
  last_response_time_ms BIGINT,
  created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE check_results (
  id BIGSERIAL PRIMARY KEY,
  monitor_id BIGINT NOT NULL REFERENCES monitors(id),
  checked_at TIMESTAMPTZ NOT NULL,
  successful BOOLEAN NOT NULL,
  status_code INTEGER,
  response_time_ms BIGINT,
  attempts INTEGER NOT NULL,
  error_message VARCHAR(1000)
);

CREATE TABLE incidents (
  id BIGSERIAL PRIMARY KEY,
  monitor_id BIGINT NOT NULL REFERENCES monitors(id),
  status VARCHAR(20) NOT NULL,
  started_at TIMESTAMPTZ NOT NULL,
  recovered_at TIMESTAMPTZ,
  outage_duration_seconds BIGINT,
  failure_message VARCHAR(1000)
);

CREATE TABLE notifications (
  id BIGSERIAL PRIMARY KEY,
  monitor_id BIGINT NOT NULL REFERENCES monitors(id),
  incident_id BIGINT REFERENCES incidents(id),
  type VARCHAR(30) NOT NULL,
  message VARCHAR(1000) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL,
  read_flag BOOLEAN NOT NULL
);
