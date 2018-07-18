CREATE TABLE IF NOT EXISTS Customers(
  customerId INTEGER NOT NULL IDENTITY,
  firstName  VARCHAR(300),
  lastName  VARCHAR(300),
  registrationId INTEGER,
  creditLimit DOUBLE,
  country  VARCHAR(300),
  PRIMARY KEY (customerId)
);
/
CREATE TABLE IF NOT EXISTS DataTypeTable(
  row_id       INTEGER,
  int_type     INTEGER,
  long_type    BIGINT,
  float_type   FLOAT,
  double_type  DOUBLE,
  boolean_type BOOLEAN,
  string_type  VARCHAR(50),
  numeric_type NUMERIC(10,3),
  decimal_type DECIMAL(10,3),
  real_type    REAL,
  tinyint_type TINYINT,
  smallint_type SMALLINT,
  clob_type    CLOB,
  binary_type  BINARY(27),
  PRIMARY KEY (row_id)
);
/
CREATE TABLE IF NOT EXISTS BlobTable(
  row_id       INTEGER,
  blob_type    BLOB,
  PRIMARY KEY (row_id)
);
/
CREATE TABLE IF NOT EXISTS DateTimeTypes(
  row_id         INTEGER,
  date_type      DATE,
  time_type      TIME,
  datetime_type  DATETIME,
  timestamp_type TIMESTAMP
);
/
CREATE TABLE IF NOT EXISTS CustomersNoKey(
  firstName  VARCHAR(300),
  lastName  VARCHAR(300),
  registrationId INTEGER,
  creditLimit DOUBLE,
  country  VARCHAR(300)
);
/
insert into DateTimeTypes (row_id, date_type, time_type, datetime_type, timestamp_type) values
  (1, '2017-02-03', '11:35:45', '2017-02-03 11:53:00', '2017-02-03 11:53:00');
/
insert into BlobTable (row_id, blob_type) values
  (1, X'77736F322062616C6C6572696E6120626C6F6220746573742E');
/
insert into BlobTable (row_id, blob_type) values
  (7, X'77736F322062616C6C6572696E6120626C6F6220746573742E');
/
insert into DataTypeTable (row_id, int_type, long_type, float_type, double_type, boolean_type, string_type,
  numeric_type, decimal_type, real_type, tinyint_type, smallint_type, clob_type, binary_type) values
  (1, 10, 9223372036854774807, 123.34, 2139095039, TRUE, 'Hello',1234.567, 1234.567, 1234.567, 1, 5555,
  CONVERT('very long text', CLOB), X'77736F322062616C6C6572696E612062696E61727920746573742E');
/
insert into DataTypeTable (row_id) values (2);
/
insert into BlobTable (row_id) values (2);
/
insert into Customers (firstName,lastName,registrationID,creditLimit,country)
  values ('Peter', 'Stuart', 1, 5000.75, 'USA');
/
insert into Customers (firstName,lastName,registrationID,creditLimit,country)
  values ('John', 'Watson', 2, 2348.93, 'UK');
/
CREATE TABLE employeeItr (id INTEGER NOT NULL, name VARCHAR(20), address VARCHAR(20));
/
INSERT INTO employeeItr VALUES (1, 'Manuri', 'Sri Lanka');
/
INSERT INTO employeeItr VALUES (2, 'Devni', 'Sri Lanka');
/
INSERT INTO employeeItr VALUES (3, 'Thurani', 'Sri Lanka');
/
CREATE TABLE employeeAdd (id INTEGER NOT NULL, name VARCHAR(20), address VARCHAR(20));
/
CREATE TABLE employeeDel (id INTEGER NOT NULL, name VARCHAR(20), address VARCHAR(20));
/
INSERT INTO employeeDel VALUES (1, 'Manuri', 'Sri Lanka');
/
INSERT INTO employeeDel VALUES (2, 'Devni', 'Sri Lanka');
/
CREATE TABLE employeeAddNegative (id INTEGER NOT NULL, name VARCHAR(20), address VARCHAR(20), PRIMARY KEY (id));
/
INSERT INTO employeeAddNegative VALUES (1, 'Manuri', 'Sri Lanka');
/
