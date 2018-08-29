CREATE TABLE IF NOT EXISTS Customers(
  customerId INT NOT NULL AUTO_INCREMENT,
  firstName  VARCHAR(300),
  lastName  VARCHAR(300),
  registrationID INT,
  creditLimit DOUBLE,
  country  VARCHAR(300),
  PRIMARY KEY (customerId)
);
/
INSERT INTO Customers (firstName,lastName,registrationID,creditLimit,country)
  VALUES ('Peter', 'Stuart', 1, 5000.75, 'USA');
/
INSERT INTO Customers (firstName,lastName,registrationID,creditLimit,country)
  VALUES ('John', 'Watson', 2, 2348.93, 'UK');
/
CREATE TABLE IF NOT EXISTS DataTypeTable(
  row_id       INT,
  int_type     INT,
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
  clob_type    TEXT,
  binary_type  BINARY(27),
  PRIMARY KEY (row_id)
);
/
INSERT INTO DataTypeTable (row_id, int_type, long_type, float_type, double_type, boolean_type, string_type,
  numeric_type, decimal_type, real_type, tinyint_type, smallint_type, clob_type, binary_type) VALUES
  (1, 10, 9223372036854774807, 123.34, 2139095039, TRUE, 'Hello',1234.567, 1234.567, 1234.567, 1, 5555,
 'very long text', X'77736F322062616C6C6572696E612062696E61727920746573742E');
/
INSERT INTO DataTypeTable (row_id) VALUES (2);
/
CREATE TABLE IF NOT EXISTS BlobTable(
  row_id       INT,
  blob_type    BLOB,
  PRIMARY KEY (row_id)
);
/
INSERT INTO BlobTable (row_id) VALUES (2);
/
INSERT INTO BlobTable VALUES (1, X'77736F322062616C6C6572696E6120626C6F6220746573742E');
/
INSERT INTO BlobTable VALUES (7, X'77736F322062616C6C6572696E6120626C6F6220746573742E');
/
CREATE TABLE IF NOT EXISTS DateTimeTypes(
  row_id         INTEGER,
  date_type      DATE,
  time_type      TIME,
  datetime_type  DATETIME,
  timestamp_type TIMESTAMP NULL
);
/
INSERT INTO DateTimeTypes (row_id, date_type, time_type, datetime_type, timestamp_type) VALUES
  (1, '2017-02-03', '11:35:45', '2017-02-03 11:53:00', '2017-02-03 11:53:00');
/
CREATE TABLE IF NOT EXISTS CustomersNoKey(
  firstName  VARCHAR(300),
  lastName  VARCHAR(300),
  registrationID INT,
  creditLimit DOUBLE,
  country  VARCHAR(300)
);
/
CREATE PROCEDURE InsertPersonData(IN p_RegID INT, IN p_PersonName VARCHAR(50))
  MODIFIES SQL DATA
  BEGIN
  INSERT INTO Customers(registrationID, firstName,lastName, creditLimit,country)
    VALUES (p_RegID, p_PersonName, p_PersonName, 25000, 'UK');
  END
/
CREATE PROCEDURE SelectPersonData()
  READS SQL DATA
  BEGIN
  SELECT firstName FROM Customers where registrationID = 1;
  END
/
CREATE PROCEDURE SelectPersonDataMultiple()
  READS SQL DATA
  BEGIN
  SELECT firstName FROM Customers where registrationID = 1;
  SELECT firstName, lastName FROM Customers where registrationID = 2;
  END
/
CREATE PROCEDURE TestOutParams (IN id INT, OUT paramInt INT, OUT paramBigInt BIGINT, OUT paramFloat FLOAT,
  OUT paramDouble DOUBLE, OUT paramBool BOOLEAN, OUT paramString VARCHAR(50),OUT paramNumeric NUMERIC(10,3),
  OUT paramDecimal DECIMAL(10,3), OUT paramReal REAL, OUT paramTinyInt TINYINT,
  OUT paramSmallInt SMALLINT, OUT paramClob TEXT, OUT paramBinary BINARY(27))
  READS SQL DATA
  BEGIN
  SELECT int_type INTO paramInt FROM DataTypeTable where row_id = id;
  SELECT long_type INTO paramBigInt FROM DataTypeTable where row_id = id;
  SELECT float_type INTO paramFloat FROM DataTypeTable where row_id = id;
  SELECT double_type INTO paramDouble FROM DataTypeTable where row_id = id;
  SELECT boolean_type INTO paramBool FROM DataTypeTable where row_id = id;
  SELECT string_type INTO paramString FROM DataTypeTable where row_id = id;
  SELECT numeric_type INTO paramNumeric FROM DataTypeTable where row_id = id;
  SELECT decimal_type INTO paramDecimal FROM DataTypeTable where row_id = id;
  SELECT real_type INTO paramReal FROM DataTypeTable where row_id = id;
  SELECT tinyint_type INTO paramTinyInt FROM DataTypeTable where row_id = id;
  SELECT smallint_type INTO paramSmallInt FROM DataTypeTable where row_id = id;
  SELECT clob_type INTO paramClob FROM DataTypeTable where row_id = id;
  SELECT binary_type INTO paramBinary FROM DataTypeTable where row_id = id;
  END
/
CREATE PROCEDURE TestINOUTParams (IN id INT, INOUT paramInt INT, INOUT paramBigInt BIGINT, INOUT paramFloat FLOAT,
  INOUT paramDouble DOUBLE, INOUT paramBool BOOLEAN, INOUT paramString VARCHAR(50),
  INOUT paramNumeric NUMERIC(10,3), INOUT paramDecimal DECIMAL(10,3), INOUT paramReal REAL, INOUT paramTinyInt TINYINT,
  INOUT paramSmallInt SMALLINT, INOUT paramClob TEXT, INOUT paramBinary BINARY(27))
  MODIFIES SQL DATA
  BEGIN
  INSERT INTO DataTypeTable (row_id, int_type, long_type, float_type, double_type, boolean_type, string_type,
     numeric_type, decimal_type, real_type, tinyint_type, smallint_type, clob_type, binary_type)
     VALUES (id, paramInt, paramBigInt, paramFloat, paramDouble, paramBool, paramString, paramNumeric, paramDecimal,
     paramReal, paramTinyInt, paramSmallInt, paramClob, paramBinary);

  SELECT int_type INTO paramInt FROM DataTypeTable where row_id = id;
  SELECT long_type INTO paramBigInt FROM DataTypeTable where row_id = id;
  SELECT float_type INTO paramFloat FROM DataTypeTable where row_id = id;
  SELECT double_type INTO paramDouble FROM DataTypeTable where row_id = id;
  SELECT boolean_type INTO paramBool FROM DataTypeTable where row_id = id;
  SELECT string_type INTO paramString FROM DataTypeTable where row_id = id;
  SELECT numeric_type INTO paramNumeric FROM DataTypeTable where row_id = id;
  SELECT decimal_type INTO paramDecimal FROM DataTypeTable where row_id = id;
  SELECT real_type INTO paramReal FROM DataTypeTable where row_id = id;
  SELECT tinyint_type INTO paramTinyInt FROM DataTypeTable where row_id = id;
  SELECT smallint_type INTO paramSmallInt FROM DataTypeTable where row_id = id;
  SELECT clob_type INTO paramClob FROM DataTypeTable where row_id = id;
  SELECT binary_type INTO paramBinary FROM DataTypeTable where row_id = id;
  END
/
CREATE PROCEDURE TestOutInOutParamsBlob (IN idOut INT, IN idInOut INT, OUT paramBlobOut BLOB, INOUT paramBlobInOut BLOB)
  READS SQL DATA
  BEGIN
  SELECT blob_type INTO paramBlobOut FROM BlobTable where row_id = idOut;

  INSERT INTO BlobTable (row_id, blob_type) VALUES (idInOut, paramBlobInOut);
  SELECT blob_type INTO paramBlobInOut FROM BlobTable where row_id = idInOut;
  END
/
CREATE PROCEDURE TestDateTimeOutParams (IN id INT, IN dateVal DATE, IN timeVal TIME, IN datetimeVal DATETIME,
  IN timestampVal TIMESTAMP, OUT dateValOUT DATE, OUT timeValOUT TIME, OUT datetmOut DATETIME, OUT timestOut TIMESTAMP)
  MODIFIES SQL DATA
  BEGIN
  INSERT INTO DateTimeTypes (row_id, date_type, time_type, datetime_type, timestamp_type) VALUES
  (id, dateVal, timeVal, datetimeVal, timestampVal);
  SELECT date_type INTO dateValOUT FROM DateTimeTypes where row_id = id;
  SELECT time_type INTO timeValOUT FROM DateTimeTypes where row_id = id;
  SELECT datetime_type INTO datetmOut FROM DateTimeTypes where row_id = id;
  SELECT timestamp_type INTO timestOut FROM DateTimeTypes where row_id = id;
  END
/
CREATE PROCEDURE TestDateINOUTParams (IN id INT, INOUT dateVal DATE, INOUT timeVal TIME, INOUT datetimeVal DATETIME,
  INOUT timestampVal TIMESTAMP)
  MODIFIES SQL DATA
  BEGIN
  INSERT INTO DateTimeTypes (row_id, date_type, time_type, datetime_type, timestamp_type) VALUES
  (id, dateVal, timeVal, datetimeVal, timestampVal);

  SELECT date_type INTO dateVal FROM DateTimeTypes where row_id = id;
  SELECT time_type INTO timeVal FROM DateTimeTypes where row_id = id;
  SELECT datetime_type INTO datetimeVal FROM DateTimeTypes where row_id = id;
  SELECT timestamp_type INTO timestampVal FROM DateTimeTypes where row_id = id;
  END
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
CREATE TABLE employeeDeleteInTrans (id INTEGER NOT NULL, name VARCHAR(20), address VARCHAR(20));
/
INSERT INTO employeeDeleteInTrans VALUES (1, 'Manuri', 'Sri Lanka');
/
INSERT INTO employeeDeleteInTrans VALUES (2, 'Devni', 'Sri Lanka');
/
CREATE TABLE employeeAddNegative (id INTEGER NOT NULL, name VARCHAR(20), address VARCHAR(20), PRIMARY KEY (id));
/
INSERT INTO employeeAddNegative VALUES (1, 'Manuri', 'Sri Lanka');
/
