-- Database: harvester
-- assumes harvester user.

-- this just needs to store emails that can be later programmatically
-- accessed (and offline).

-- DROP DATABASE harvester;

CREATE DATABASE harvester
  WITH OWNER = postgres
       ENCODING = 'UTF8'
       TABLESPACE = pg_default
       LC_COLLATE = 'en_US.UTF-8'
       LC_CTYPE = 'en_US.UTF-8'
       CONNECTION LIMIT = -1;

-- Table: raw_mail

-- DROP TABLE raw_mail;

CREATE TABLE raw_mail
(
  id serial NOT NULL,
  mid text NOT NULL,
  sent timestamp with time zone NOT NULL,
  addr_from text NOT NULL,
  addr_to text NOT NULL,
  received timestamp with time zone NOT NULL,
  content_type text NOT NULL,
  multipart boolean NOT NULL,
  body text,
  subject text,
  CONSTRAINT raw_mail_pkey PRIMARY KEY (id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE raw_mail
  OWNER TO harvester;
