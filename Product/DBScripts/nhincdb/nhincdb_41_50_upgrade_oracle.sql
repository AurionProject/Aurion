-- Drop aggregator tables
DROP TABLE nhincuser.agg_transaction;
DROP TABLE nhincuser.agg_message_results;

-- Drop lift tables
DROP TABLE nhincuser.gateway_lift_message;
DROP TABLE nhincuser.transfer_data;


--Drop performance table
DROP TABLE nhincuser.auditperformance;

-- TABLE MODIFICATIONS
-- Add Sample
-- ALTER TABLE hr.admin_emp ADD (bonus NUMBER (7,2));
-- Modify sample
-- ALTER TABLE countries MODIFY (duty_pct NUMBER(3,2)); 
-- Rename sample
-- ALTER TABLE customers RENAME COLUMN credit_limit TO credit_amount;
-- Drop columns sample
-- ALTER TABLE t1 DROP (pk, fk, c1);

-- Update column lengths
ALTER TABLE nhincuser.aa_to_home_community_mapping MODIFY assigningauthorityid VARCHAR2(64);
ALTER TABLE nhincuser.aa_to_home_community_mapping MODIFY homecommunityid VARCHAR2(64);
ALTER TABLE nhincuser.auditrepository MODIFY receiverPatientId VARCHAR2(128);
ALTER TABLE nhincuser.auditrepository MODIFY senderPatientId VARCHAR2(128);
ALTER TABLE nhincuser.document MODIFY PatientId VARCHAR2(128) default NULL;
ALTER TABLE nhincuser.correlatedidentifiers MODIFY PatientId VARCHAR2(128);
ALTER TABLE nhincuser.correlatedidentifiers MODIFY PatientAssigningAuthorityId VARCHAR2(64);
ALTER TABLE nhincuser.correlatedidentifiers MODIFY CorrelatedPatientId VARCHAR2(128);
ALTER TABLE nhincuser.correlatedidentifiers MODIFY CorrelatedPatientAssignAuthId VARCHAR2(64);

-- Add On-Demand columns
ALTER TABLE nhincuser.document ADD OnDemand NUMBER(1) default 0 NOT NULL;
ALTER TABLE nhincuser.document ADD NewDocumentUniqueId VARCHAR2(128) default NULL;
ALTER TABLE nhincuser.document ADD NewRepositoryUniqueId VARCHAR2(128) default NULL;

-- Update asyncmsgrepo  with column changes and additions
ALTER TABLE nhincuser.asyncmsgrepo MODIFY ServiceName VARCHAR2(45) DEFAULT NULL;
ALTER TABLE nhincuser.asyncmsgrepo ADD ResponseTime DATE DEFAULT NULL;
ALTER TABLE nhincuser.asyncmsgrepo ADD Duration NUMBER(19) DEFAULT 0;
ALTER TABLE nhincuser.asyncmsgrepo ADD Direction VARCHAR2(10) DEFAULT NULL;
ALTER TABLE nhincuser.asyncmsgrepo ADD CommunityId VARCHAR2(100) DEFAULT NULL;
ALTER TABLE nhincuser.asyncmsgrepo ADD Status VARCHAR2(45) DEFAULT NULL;
ALTER TABLE nhincuser.asyncmsgrepo ADD ResponseType VARCHAR2(10) DEFAULT NULL;
ALTER TABLE nhincuser.asyncmsgrepo ADD Reserved VARCHAR2(100) DEFAULT NULL;
ALTER TABLE nhincuser.asyncmsgrepo ADD RspData BLOB DEFAULT NULL;
ALTER TABLE nhincuser.asyncmsgrepo ADD AckData BLOB DEFAULT NULL;

-- NEW TABLES

CREATE TABLE nhincuser.pddeferredcorrelation (
  Id NUMBER(10) NOT NULL,
  MessageId VARCHAR2(100) NOT NULL,
  AssigningAuthorityId VARCHAR2(64) NOT NULL,
  PatientId VARCHAR2(128) NOT NULL,
  CreationTime DATE NOT NULL,
  PRIMARY KEY (Id)
);

CREATE TABLE nhincuser.patient (
  patientId number(11) NOT NULL,
  dateOfBirth DATE NULL,
  gender CHAR(2) NULL,
  ssn CHAR(9) NULL,
  PRIMARY KEY (patientId)
);

CREATE TABLE nhincuser.identifier (
  identifierId number(11) NOT NULL,
  patientId number(11) NOT NULL,
  id varchar2(64) NULL,
  organizationId varchar2(64) NULL,
  PRIMARY KEY (identifierId)
);

CREATE TABLE nhincuser.personname (
  personnameId number(11) NOT NULL,
  patientId number(11) NOT NULL,
  prefix varchar2(64) NULL,
  firstName varchar2(64) NULL,
  middleName varchar2(64) NULL,
  lastName varchar2(64) NULL,
  suffix varchar2(64) NULL,
  PRIMARY KEY (personnameId)
);

CREATE TABLE nhincuser.address (
  addressId number(11) NOT NULL,
  patientId number(11) NOT NULL,
  street1 varchar2(128) NULL,
  street2 varchar2(128) NULL,
  city varchar2(128) NULL,
  state varchar2(128) NULL,
  postal varchar2(45) NULL,
  PRIMARY KEY (addressId)
);

CREATE TABLE nhincuser.phonenumber (
  phonenumberId number(11) NOT NULL,
  patientId number(11) NOT NULL,
  value varchar2(64) NULL,
  PRIMARY KEY (phonenumberId)
);

CREATE TABLE nhincuser.log (
    dt DATE DEFAULT sysdate NOT NULL,
    context VARCHAR2(100) DEFAULT NULL,
    logLevel VARCHAR2(10) DEFAULT NULL,
    class VARCHAR2(500) DEFAULT NULL,
    message CLOB
);

CREATE TABLE nhincuser.transactionrepository (
    id NUMBER(19) NOT NULL,
    transactionId VARCHAR2(100) NOT NULL,
    messageId VARCHAR2(100) NOT NULL,
    transactionTime DATE NULL,
    PRIMARY KEY (id)
);

CREATE TABLE nhincuser.event (
  id NUMBER(19) NOT NULL,
  name VARCHAR2(100) NOT NULL,
  description CLOB,
  transactionId VARCHAR2(100),
  messageId VARCHAR2(100),
  eventTime DATE,
  PRIMARY KEY (id) 
);

