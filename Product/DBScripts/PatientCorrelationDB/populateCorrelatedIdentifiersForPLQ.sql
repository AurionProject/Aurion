# Script to Populate Correlation Table for testing of ITI-56 on Aurion5
USE patientcorrelationdb
DELETE FROM correlatedidentifiers where correlationId > 1000;

INSERT INTO   correlatedidentifiers values (1001,'3.3',          '445577', '1.1',      '23619', '20160101020202');
INSERT INTO   correlatedidentifiers values (1002,'12.2.18',      '301164', '1.1',     '783295', '20160101020202');
INSERT INTO   correlatedidentifiers values (1003,'12.2.18.100',  '869425', '1.1',     '783295', '20160101020202');
INSERT INTO   correlatedidentifiers values (1004,'19.21.4',      '309823', '1.1',    '1114927', '20160101020202');
# Test Reverse Insertion
INSERT INTO   correlatedidentifiers values (1005, '1.1',         '9016525', '39.7.12', '674192', '20160101020202');

