CREATE TABLE IF NOT EXISTS Customers(
  customerId INTEGER NOT NULL IDENTITY,
  firstName  VARCHAR(300),
  lastName  VARCHAR(300),
  registrationID INTEGER,
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
  blob_type    BLOB,
  binary_type  BINARY(27),
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
insert into DateTimeTypes (row_id, date_type, time_type, datetime_type, timestamp_type) values
  (1, '2017-02-03', '11:35:45', '2017-02-03 11:53:00', '2017-02-03 11:53:00');
/
insert into DataTypeTable (row_id, int_type, long_type, float_type, double_type, boolean_type, string_type,
  numeric_type, decimal_type, real_type, tinyint_type, smallint_type, clob_type, blob_type, binary_type) values
  (1, 10, 9223372036854774807, 123.34, 2139095039, TRUE, 'Hello',1234.567, 1234.567, 1234.567, 1, 5555,
  CONVERT('very long text', CLOB), X'77736F322062616C6C6572696E6120626C6F6220746573742E',
  X'77736F322062616C6C6572696E612062696E61727920746573742E');
/
insert into DataTypeTable (row_id) values (2);
/
insert into Customers (firstName,lastName,registrationID,creditLimit,country)
  values ('Peter', 'Stuart', 1, 5000.75, 'USA');
/
insert into Customers (firstName,lastName,registrationID,creditLimit,country)
  values ('John', 'Watson', 2, 2348.93, 'UK');
/
CREATE PROCEDURE InsertPersonData(IN p_RegID INTEGER, IN p_PersonName VARCHAR(50))
  MODIFIES SQL DATA
  BEGIN ATOMIC
  INSERT INTO Customers(registrationID, firstName,lastName, creditLimit,country)
    VALUES (p_RegID, p_PersonName, p_PersonName, 25000, 'UK');
  END
/
CREATE PROCEDURE SelectPersonData()
  READS SQL DATA DYNAMIC RESULT SETS 1
  BEGIN ATOMIC
  DECLARE result CURSOR WITH RETURN FOR SELECT firstName FROM Customers where registrationID = 1 FOR READ ONLY;
  open result;
  END
/
CREATE PROCEDURE SelectPersonDataMultiple()
  READS SQL DATA DYNAMIC RESULT SETS 2
  BEGIN ATOMIC
  DECLARE result1 CURSOR WITH RETURN FOR SELECT firstName FROM Customers where registrationID = 1 FOR READ ONLY;
  DECLARE result2 CURSOR WITH RETURN FOR SELECT firstName, lastName FROM Customers where registrationID = 2 FOR READ
  ONLY;
  open result1;
  open result2;
  END
/
CREATE PROCEDURE TestOutParams (IN id INT, OUT paramInt INT, OUT paramBigInt BIGINT, OUT paramFloat FLOAT,
  OUT paramDouble DOUBLE, OUT paramBool BOOLEAN, OUT paramString VARCHAR(50),OUT paramNumeric NUMERIC(10,3),
  OUT paramDecimal DECIMAL(10,3), OUT paramReal REAL, OUT paramTinyInt TINYINT,
  OUT paramSmallInt SMALLINT, OUT paramClob CLOB, OUT paramBlob BLOB, OUT paramBinary BINARY(27))
  READS SQL DATA
  BEGIN ATOMIC
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
  SELECT blob_type INTO paramBlob FROM DataTypeTable where row_id = id;
  SELECT binary_type INTO paramBinary FROM DataTypeTable where row_id = id;
  END
/
CREATE PROCEDURE TestINOUTParams (IN id INT, INOUT paramInt INT, INOUT paramBigInt BIGINT, INOUT paramFloat FLOAT,
  INOUT paramDouble DOUBLE, INOUT paramBool BOOLEAN, INOUT paramString VARCHAR(50),
  INOUT paramNumeric NUMERIC(10,3), INOUT paramDecimal DECIMAL(10,3), INOUT paramReal REAL, INOUT paramTinyInt TINYINT,
  INOUT paramSmallInt SMALLINT, INOUT paramClob CLOB, INOUT paramBlob BLOB, INOUT paramBinary BINARY(27))
  MODIFIES SQL DATA
  BEGIN ATOMIC
  INSERT INTO DataTypeTable (row_id, int_type, long_type, float_type, double_type, boolean_type, string_type,
     numeric_type, decimal_type, real_type, tinyint_type, smallint_type, clob_type, blob_type, binary_type)
     VALUES (id, paramInt, paramBigInt, paramFloat, paramDouble, paramBool, paramString, paramNumeric, paramDecimal,
     paramReal, paramTinyInt, paramSmallInt, paramClob, paramBlob, paramBinary);

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
  SELECT blob_type INTO paramBlob FROM DataTypeTable where row_id = id;
  SELECT binary_type INTO paramBinary FROM DataTypeTable where row_id = id;
  END
/
CREATE TABLE IF NOT EXISTS ArrayTypes(
  row_id        INTEGER,
  int_array     INTEGER ARRAY,
  long_array    BIGINT ARRAY,
  float_array   FLOAT ARRAY,
  double_array  DOUBLE ARRAY,
  boolean_array BOOLEAN ARRAY,
  string_array  VARCHAR(50) ARRAY,
  PRIMARY KEY (row_id)
);
/
INSERT INTO ArrayTypes (row_id, int_array, long_array, float_array, double_array, boolean_array, string_array)
  VALUES 1, ARRAY[1, 2, 3], ARRAY [100000000, 200000000, 300000000], ARRAY [245.23, 5559.49, 8796.123],
  ARRAY [245.23, 5559.49, 8796.123], ARRAY [TRUE, FALSE, TRUE], ARRAY ['Hello', 'Ballerina'];
/
CREATE PROCEDURE TestArrayOutParams (OUT intArray INTEGER ARRAY, OUT longArray BIGINT ARRAY, OUT floatArray FLOAT ARRAY,
  OUT doubleArray DOUBLE ARRAY, OUT boolArray BOOLEAN ARRAY, OUT varcharArray VARCHAR(50) ARRAY)
  READS SQL DATA
  BEGIN ATOMIC
  SELECT int_array INTO intArray FROM ArrayTypes where row_id = 1;
  SELECT long_array INTO longArray FROM ArrayTypes where row_id = 1;
  SELECT float_array INTO floatArray FROM ArrayTypes where row_id = 1;
  SELECT double_array INTO doubleArray FROM ArrayTypes where row_id = 1;
  SELECT boolean_array INTO boolArray FROM ArrayTypes where row_id = 1;
  SELECT string_array INTO varcharArray FROM ArrayTypes where row_id = 1;
  END
/
CREATE PROCEDURE TestDateTimeOutParams (IN id INT, IN dateVal DATE, IN timeVal TIME, IN datetimeVal DATETIME,
  IN timestampVal TIMESTAMP, OUT dateValOUT DATE, OUT timeValOUT TIME, OUT datetmOut DATETIME, OUT timestOut TIMESTAMP)
  MODIFIES SQL DATA
  BEGIN ATOMIC
  insert into DateTimeTypes (row_id, date_type, time_type, datetime_type, timestamp_type) values
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
  BEGIN ATOMIC
  insert into DateTimeTypes (row_id, date_type, time_type, datetime_type, timestamp_type) values
  (id, dateVal, timeVal, datetimeVal, timestampVal);

  SELECT date_type INTO dateVal FROM DateTimeTypes where row_id = id;
  SELECT time_type INTO timeVal FROM DateTimeTypes where row_id = id;
  SELECT datetime_type INTO datetimeVal FROM DateTimeTypes where row_id = id;
  SELECT timestamp_type INTO timestampVal FROM DateTimeTypes where row_id = id;
  END
/
CREATE PROCEDURE TestArrayINOutParams (IN id INT, OUT insertedCount INTEGER, INOUT intArray INTEGER ARRAY,
  INOUT longArray BIGINT ARRAY, INOUT floatArray FLOAT ARRAY, INOUT doubleArray DOUBLE ARRAY,
  INOUT boolArray BOOLEAN ARRAY, INOUT varcharArray VARCHAR(50) ARRAY)
  MODIFIES SQL DATA
  BEGIN ATOMIC
  INSERT INTO ArrayTypes (row_id, int_array, long_array, float_array, double_array, boolean_array, string_array)
  VALUES (id, intArray, longArray, floatArray, doubleArray, boolArray, varcharArray);

  SELECT count(*) INTO insertedCount from ArrayTypes where row_id = id;

  SELECT int_array INTO intArray FROM ArrayTypes where row_id = 1;
  SELECT long_array INTO longArray FROM ArrayTypes where row_id = 1;
  SELECT float_array INTO floatArray FROM ArrayTypes where row_id = 1;
  SELECT double_array INTO doubleArray FROM ArrayTypes where row_id = 1;
  SELECT boolean_array INTO boolArray FROM ArrayTypes where row_id = 1;
  SELECT string_array INTO varcharArray FROM ArrayTypes where row_id = 1;
  END
/
CREATE TYPE customtype AS INTEGER;
/
CREATE TABLE structdatatable(id INTEGER, structdata customtype);
/
INSERT INTO structdatatable(id,structdata) VALUES (1,10);
/
CREATE PROCEDURE TestStructOut (OUT var customtype)
  READS SQL DATA
  BEGIN ATOMIC
  SELECT structdata INTO var from structdatatable where id = 1;
  END
/
CREATE PROCEDURE TestStructInOut (OUT countVal INTEGER, INOUT var customtype)
  MODIFIES SQL DATA
  BEGIN ATOMIC
  INSERT INTO structdatatable(id,structdata) VALUES (2,var);
  select count(*) into countVal from structdatatable where id = 2;
 SELECT structdata INTO var from structdatatable where id = 1;
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
CREATE TABLE employeeAddNegative (id INTEGER NOT NULL, name VARCHAR(20), address VARCHAR(20), PRIMARY KEY (id));
/
INSERT INTO employeeAddNegative VALUES (1, 'Manuri', 'Sri Lanka');
/
