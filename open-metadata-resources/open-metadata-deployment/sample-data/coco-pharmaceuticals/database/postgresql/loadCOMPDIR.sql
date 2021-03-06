-- SPDX-License-Identifier: Apache-2.0
-- Copyright Contributors to the ODPi Egeria project.
\c "COMPDIR";

CREATE TABLE IF NOT EXISTS "CONTACTLIST" (
  "RECID" INT NOT NULL PRIMARY KEY,
  "CONTACTTYPE" CHAR NOT NULL,
  "FIRSTNAME" VARCHAR(50) NOT NULL,
  "LASTNAME" VARCHAR(50) NOT NULL,
  "COMPANY" VARCHAR(50) NOT NULL,
  "JOBTITLE" VARCHAR(80) NOT NULL,
  "WORKLOCATION" INT NOT NULL
);

CREATE TABLE IF NOT EXISTS "CONTACTEMAIL" (
  "REDIF" INT NOT NULL REFERENCES "CONTACTLIST",
  "ETYPE" CHAR NOT NULL,
  "EMAIL" VARCHAR(120) NOT NULL
);

CREATE TABLE IF NOT EXISTS "CONTACTPHONE" (
  "RECID" INT NOT NULL REFERENCES "CONTACTLIST",
  "CONTACTTYPE" CHAR NOT NULL,
  "NUMBER" VARCHAR(40) NOT NULL
);

delete from "CONTACTPHONE";
delete from "CONTACTEMAIL";
delete from "CONTACTLIST";

\copy "CONTACTLIST" from '{{ egeria_samples_cocopharma_targets.files }}/CompDir-ContactList.csv' with csv header DELIMITER ';';
\copy "CONTACTEMAIL" from '{{ egeria_samples_cocopharma_targets.files }}/CompDir-ContactEmail.csv' with csv header DELIMITER ';';
\copy "CONTACTPHONE" from '{{ egeria_samples_cocopharma_targets.files }}/CompDir-ContactPhone.csv' with csv header DELIMITER ';';
