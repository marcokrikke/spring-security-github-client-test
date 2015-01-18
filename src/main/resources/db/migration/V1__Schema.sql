CREATE TABLE PUBLIC.USER (
    id IDENTITY,
    username        VARCHAR(256),
    github_username VARCHAR(256),
    name            VARCHAR(256),
    email           VARCHAR(256),
    avatar_url      VARCHAR(256)
);

ALTER TABLE PUBLIC.USER ADD CONSTRAINT unique_github_username UNIQUE (github_username);
ALTER TABLE PUBLIC.USER ADD CONSTRAINT unique_username UNIQUE (username);