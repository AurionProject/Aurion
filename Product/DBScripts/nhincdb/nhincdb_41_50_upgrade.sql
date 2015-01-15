-- DELETE SCHEMAS

-- Drop aggregator Database
drop database aggregator;

-- Drop lift Database
drop database lift;

--Drop performance Database
drop database performance;

-- TABLE MODIFICATIONS

-- Update IDs length
ALTER TABLE `assigningauthoritydb`.`aa_to_home_community_mapping` MODIFY assigningauthorityid VARCHAR(64) NOT NULL;
ALTER TABLE `assigningauthoritydb`.`aa_to_home_community_mapping` MODIFY homecommunityid VARCHAR(64) NOT NULL;
ALTER TABLE `auditrepo`.`auditrepository` MODIFY receiverPatientId VARCHAR(128);
ALTER TABLE `auditrepo`.`auditrepository` MODIFY senderPatientId VARCHAR(128);
ALTER TABLE `docrepository`.`document` MODIFY PatientId VARCHAR(128) default NULL COMMENT 'Format of HL7 2.x CX';
ALTER TABLE `patientcorrelationdb`.`correlatedidentifiers` MODIFY PatientId VARCHAR(128) NOT NULL;
ALTER TABLE `patientcorrelationdb`.`correlatedidentifiers` MODIFY PatientAssigningAuthorityId VARCHAR(64) NOT NULL;
ALTER TABLE `patientcorrelationdb`.`correlatedidentifiers` MODIFY CorrelatedPatientId VARCHAR(128) NOT NULL;
ALTER TABLE `patientcorrelationdb`.`correlatedidentifiers` MODIFY CorrelatedPatientAssignAuthId VARCHAR(64) NOT NULL;
ALTER TABLE `patientcorrelationdb`.`pddeferredcorrelation` MODIFY AssigningAuthorityId VARCHAR(64) NOT NULL;
ALTER TABLE `patientcorrelationdb`.`pddeferredcorrelation` MODIFY PatientId VARCHAR(128) NOT NULL;

-- Add On-Demand columns
ALTER TABLE `docrepository`.`document` ADD OnDemand tinyint(1) NOT NULL default 0 COMMENT 'Indicate whether document is dynamic (true or 1) or static (false or 0).';
ALTER TABLE `docrepository`.`document` ADD NewDocumentUniqueId varchar(128) default NULL;
ALTER TABLE `docrepository`.`document` ADD NewRepositoryUniqueId varchar(128) default NULL;

-- Update asyncmsgrepo  with column changes and additions
ALTER TABLE `asyncmsgs`.`asyncmsgrepo` MODIFY ServiceName VARCHAR(45) NULL DEFAULT NULL;
ALTER TABLE `asyncmsgs`.`asyncmsgrepo` MODIFY MsgData LONGBLOB NULL DEFAULT NULL;
ALTER TABLE `asyncmsgs`.`asyncmsgrepo` ADD ResponseTime DATETIME NULL DEFAULT NULL;
ALTER TABLE `asyncmsgs`.`asyncmsgrepo` ADD Duration BIGINT NULL DEFAULT 0;
ALTER TABLE `asyncmsgs`.`asyncmsgrepo` ADD Direction VARCHAR(10) NULL DEFAULT NULL;
ALTER TABLE `asyncmsgs`.`asyncmsgrepo` ADD CommunityId VARCHAR(100) NULL DEFAULT NULL;
ALTER TABLE `asyncmsgs`.`asyncmsgrepo` ADD Status VARCHAR(45) NULL DEFAULT NULL;
ALTER TABLE `asyncmsgs`.`asyncmsgrepo` ADD ResponseType VARCHAR(10) NULL DEFAULT NULL;
ALTER TABLE `asyncmsgs`.`asyncmsgrepo` ADD Reserved VARCHAR(100) NULL DEFAULT NULL;
ALTER TABLE `asyncmsgs`.`asyncmsgrepo` ADD RspData LONGBLOB NULL DEFAULT NULL;
ALTER TABLE `asyncmsgs`.`asyncmsgrepo` ADD AckData LONGBLOB NULL DEFAULT NULL;

-- Spelling Corrections
ALTER TABLE `docrepository`.`document` MODIFY documentid int(11) NOT NULL COMMENT 'Foreign key to document table';

-- NEW TABLES IN EXISTING SCHEMAS

CREATE TABLE patientcorrelationdb.pddeferredcorrelation (
  Id INT(10) UNSIGNED NOT NULL AUTO_INCREMENT,
  MessageId VARCHAR(100) NOT NULL,
  AssigningAuthorityId varchar(64) NOT NULL,
  PatientId varchar(128) NOT NULL,
  CreationTime DATETIME NOT NULL,
  PRIMARY KEY (Id)
);

-- NEW SCHEMAS

-- begin patientdb
CREATE DATABASE patientdb;

CREATE TABLE patientdb.patient (
  patientId BIGINT NOT NULL AUTO_INCREMENT,
  dateOfBirth DATE NULL,
  gender CHAR(2) NULL,
  ssn CHAR(9) NULL,
  PRIMARY KEY (patientId),
  UNIQUE INDEX patientId_UNIQUE (patientId ASC) )
COMMENT = 'Patient Repository';

CREATE TABLE patientdb.identifier (
  identifierId BIGINT NOT NULL AUTO_INCREMENT,
  patientId BIGINT NOT NULL,
  id VARCHAR(64) NULL,
  organizationId VARCHAR(64) NULL,
  PRIMARY KEY (identifierId),
  UNIQUE INDEX identifierrId_UNIQUE (identifierId ASC),
  INDEX fk_identifier_patient (patientId ASC),
  CONSTRAINT fk_identifier_patient
    FOREIGN KEY (patientId )
    REFERENCES patientdb.patient (patientId )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
COMMENT = 'Identifier definitions';

CREATE TABLE patientdb.personname (
  personnameId BIGINT NOT NULL AUTO_INCREMENT,
  patientId BIGINT NOT NULL,
  prefix VARCHAR(64) NULL,
  firstName VARCHAR(64) NULL,
  middleName VARCHAR(64) NULL,
  lastName VARCHAR(64) NULL,
  suffix VARCHAR(64) NULL,
  PRIMARY KEY (personnameId),
  UNIQUE INDEX personnameId_UNIQUE (personnameId ASC),
  INDEX fk_personname_patient (patientId ASC),
  CONSTRAINT fk_personname_patient
    FOREIGN KEY (patientId )
    REFERENCES patientdb.patient (patientId )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
COMMENT = 'Person Names';

CREATE TABLE patientdb.address (
  addressId BIGINT NOT NULL AUTO_INCREMENT,
  patientId BIGINT NOT NULL,
  street1 VARCHAR(128) NULL,
  street2 VARCHAR(128) NULL,
  city VARCHAR(128) NULL,
  state VARCHAR(128) NULL,
  postal VARCHAR(45) NULL,
  PRIMARY KEY (addressId),
  UNIQUE INDEX addressId_UNIQUE (addressId ASC),
  INDEX fk_address_patient (patientId ASC),
  CONSTRAINT fk_address_patient
    FOREIGN KEY (patientId )
    REFERENCES patientdb.patient (patientId )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
COMMENT = 'Addresses';

CREATE TABLE patientdb.phonenumber (
  phonenumberId BIGINT NOT NULL AUTO_INCREMENT,
  patientId BIGINT NOT NULL,
  value VARCHAR(64) NULL,
  PRIMARY KEY (phonenumberId),
  UNIQUE INDEX phonenumberId_UNIQUE (phonenumberId ASC),
  INDEX fk_phonenumber_patient (patientId ASC),
  CONSTRAINT fk_phonenumber_patient
    FOREIGN KEY (patientId )
    REFERENCES patientdb.patient (patientId )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
COMMENT = 'Phone Numbers';

GRANT SELECT,INSERT,UPDATE,DELETE ON patientdb.* to nhincuser;
-- end patientdb

-- begin transrepo

CREATE DATABASE transrepo;

CREATE TABLE transrepo.transactionrepository (
    id BIGINT NOT NULL AUTO_INCREMENT,
    transactionId VARCHAR(100) NOT NULL,
    messageId VARCHAR(100) NOT NULL,
    transactionTime TIMESTAMP NULL,
    PRIMARY KEY (id),
    INDEX messageId_idx (messageId),
    UNIQUE transID_UNIQUE (transactionId, messageId) )
COMMENT = 'Message Transaction Repository';

GRANT SELECT,INSERT,UPDATE,DELETE ON transrepo.* to nhincuser;
-- end transrepo

-- begin eventdb

CREATE DATABASE eventdb;

CREATE TABLE eventdb.event (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  description longtext,
  transactionId VARCHAR(100),
  messageId VARCHAR(100),
  eventTime TIMESTAMP,
  PRIMARY KEY (id) )
COMMENT = 'Event Logging';

-- end eventdb
