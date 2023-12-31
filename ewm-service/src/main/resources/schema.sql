CREATE TABLE IF NOT EXISTS users (
  id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
  name VARCHAR(250) NOT NULL,
  email VARCHAR(254) NOT NULL,
  CONSTRAINT pk_user PRIMARY KEY (id),
  CONSTRAINT UQ_USER_EMAIL UNIQUE (email)
);

ALTER TABLE users ALTER COLUMN id RESTART WITH 1;

CREATE TABLE IF NOT EXISTS categories (
  id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
  name VARCHAR(50) NOT NULL,
  CONSTRAINT pk_category PRIMARY KEY (id),
  CONSTRAINT UQ_CATEGORY_NAME UNIQUE (name)
);

ALTER TABLE categories ALTER COLUMN id RESTART WITH 1;

CREATE TABLE IF NOT EXISTS events (
  id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
  annotation VARCHAR(2000) NOT NULL,
  category_id BIGINT NOT NULL,
  confirmed_requests BIGINT,
  created_on TIMESTAMP WITHOUT TIME ZONE,
  description VARCHAR(7000) NOT NULL,
  event_date TIMESTAMP WITHOUT TIME ZONE,
  initiator_id BIGINT NOT NULL,
  lat REAL NOT NULL,
  lon REAL NOT NULL,
  paid BOOLEAN NOT NULL,
  participant_limit INT NOT NULL,
  published_on TIMESTAMP WITHOUT TIME ZONE,
  request_moderation BOOLEAN NOT NULL,
  state VARCHAR(64) NOT NULL,
  title VARCHAR(120) NOT NULL,
  views BIGINT,
  CONSTRAINT pk_event PRIMARY KEY (id),
  CONSTRAINT fk_category_id
            FOREIGN KEY (category_id)
            REFERENCES categories (id) ON DELETE CASCADE,
    CONSTRAINT fk_initiator_id
            FOREIGN KEY (initiator_id)
            REFERENCES users (id) ON DELETE CASCADE
);

ALTER TABLE events ALTER COLUMN id RESTART WITH 1;

CREATE TABLE IF NOT EXISTS requests (
  id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
  event_id BIGINT NOT NULL,
  requester_id BIGINT NOT NULL,
  created TIMESTAMP WITHOUT TIME ZONE,
  status VARCHAR(64) NOT NULL,
  CONSTRAINT pk_request PRIMARY KEY (id),
  CONSTRAINT fk_event_id
            FOREIGN KEY (event_id)
            REFERENCES events (id) ON DELETE CASCADE,
    CONSTRAINT fk_requester_id
            FOREIGN KEY (requester_id)
            REFERENCES users (id) ON DELETE CASCADE
);

ALTER TABLE requests ALTER COLUMN id RESTART WITH 1;
ALTER TABLE requests ADD COLUMN IF NOT EXISTS created TIMESTAMP WITHOUT TIME ZONE;

CREATE TABLE IF NOT EXISTS compilations (
  id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
  title VARCHAR(250) NOT NULL,
  pinned BOOLEAN NOT NULL,
  CONSTRAINT pk_compilation PRIMARY KEY (id),
  CONSTRAINT UQ_COMPILATION_TITLE UNIQUE (title)
);

ALTER TABLE compilations ALTER COLUMN id RESTART WITH 1;

CREATE TABLE IF NOT EXISTS events_compilations (
  event_id BIGINT NOT NULL,
  compilation_id BIGINT NOT NULL,
  CONSTRAINT pk_event_compilation primary key (event_id, compilation_id),
  CONSTRAINT fk_event_id
              FOREIGN KEY (event_id)
              REFERENCES events (id) ON DELETE CASCADE,
      CONSTRAINT fk_compilation_id
              FOREIGN KEY (compilation_id)
              REFERENCES compilations (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS comments (
  id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
  text VARCHAR(3000) NOT NULL,
  event_id BIGINT NOT NULL,
  author_id BIGINT NOT NULL,
  state_comment VARCHAR(64) NOT NULL,
  published_on TIMESTAMP WITHOUT TIME ZONE,
  CONSTRAINT pk_comment PRIMARY KEY (id),
  CONSTRAINT fk_event_id_comments
            FOREIGN KEY (event_id)
            REFERENCES events (id) ON DELETE CASCADE,
    CONSTRAINT fk_author_id
            FOREIGN KEY (author_id)
            REFERENCES users (id) ON DELETE CASCADE
);

ALTER TABLE comments ALTER COLUMN id RESTART WITH 1;