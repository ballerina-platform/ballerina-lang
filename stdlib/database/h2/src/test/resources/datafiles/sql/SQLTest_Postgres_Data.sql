CREATE TABLE IF NOT EXISTS Customers(
  CUSTOMERID SERIAL NOT NULL,
  FIRSTNAME  VARCHAR(300),
  LASTNAME  VARCHAR(300),
  REGISTRATIONID INTEGER,
  CREDITLIMIT  double precision,
  COUNTRY  VARCHAR(300),
  PRIMARY KEY (CUSTOMERID)
);
/
INSERT INTO Customers (firstName,lastName,registrationID,creditLimit,country)
  VALUES ('Peter', 'Stuart', 1, 5000.75, 'USA');
/
INSERT INTO Customers (firstName,lastName,registrationID,creditLimit,country)
  VALUES ('John', 'Watson', 2, 2348.93, 'UK');
/
CREATE TABLE IF NOT EXISTS DataTypeTable(
  ROW_ID       INTEGER,
  INT_TYPE     INTEGER,
  LONG_TYPE    BIGINT,
  FLOAT_TYPE   FLOAT(4),
  DOUBLE_TYPE  DOUBLE PRECISION,
  BOOLEAN_TYPE BOOLEAN,
  STRING_TYPE  VARCHAR(50),
  NUMERIC_TYPE NUMERIC(10,3),
  DECIMAL_TYPE DECIMAL(10,3),
  REAL_TYPE    REAL,
  TINYINT_TYPE SMALLINT,
  SMALLINT_TYPE SMALLINT,
  CLOB_TYPE    TEXT,
  BINARY_TYPE  BYTEA,
  PRIMARY KEY (row_id)
);
/
INSERT INTO DataTypeTable (row_id, int_type, long_type, float_type, double_type, boolean_type, string_type,
  numeric_type, decimal_type, real_type, tinyint_type, smallint_type, clob_type, binary_type) VALUES
  (1, 10, 9223372036854774807, 123.34, 2139095039, TRUE, 'Hello',1234.567, 1234.567, 1234.567, 1, 5555,
  'very long text', E'\\x77736F322062616C6C6572696E612062696E61727920746573742E');
/
INSERT INTO DataTypeTable (row_id) VALUES (2);
/
CREATE TABLE IF NOT EXISTS DateTimeTypes(
  ROW_ID         INTEGER,
  DATE_TYPE      TIMESTAMP,
  TIME_TYPE      TIME,
  DATETIME_TYPE  TIMESTAMP,
  TIMESTAMP_TYPE TIMESTAMPTZ
);
/
INSERT INTO DateTimeTypes (row_id, date_type, time_type, datetime_type, timestamp_type) VALUES
  (1, '2017-02-03', '11:35:45', '2017-02-03 11:53:00', '2017-02-03 11:53:00');
/
CREATE TABLE IF NOT EXISTS CustomersNoKey(
  FIRSTNAME  VARCHAR(300),
  LASTNAME  VARCHAR(300),
  REGISTRATIONID INTEGER,
  CREDITLIMIT  double precision,
  COUNTRY  VARCHAR(300)
);
/
CREATE FUNCTION InsertPersonData(IN p_RegID INT, IN p_PersonName VARCHAR(50)) RETURNS VOID AS '
    BEGIN
    INSERT INTO Customers(registrationID, firstName,lastName, creditLimit,country)
    VALUES (p_RegID, p_PersonName, p_PersonName, 25000, ''UK'');
    END;
' LANGUAGE plpgsql;
/
CREATE FUNCTION SelectPersonData() RETURNS refcursor AS '
    DECLARE ref refcursor;
    BEGIN
    OPEN ref FOR SELECT firstName FROM Customers where registrationID = 1;
    RETURN ref;
    END;
' LANGUAGE plpgsql;
/
CREATE FUNCTION TestOutParams (IN id BIGINT, OUT paramInt INTEGER, OUT paramBigInt BIGINT, OUT paramFloat FLOAT(4),
  OUT paramDouble DOUBLE PRECISION, OUT paramBool BOOLEAN, OUT paramString VARCHAR(50),OUT paramNumeric NUMERIC(10,3),
  OUT paramDecimal DECIMAL(10,3), OUT paramReal REAL, OUT paramTinyInt SMALLINT,
  OUT paramSmallInt SMALLINT, OUT paramClob TEXT, OUT paramBinary BYTEA) AS '
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
' LANGUAGE plpgsql;
/
CREATE FUNCTION TestINOUTParams (IN id INTEGER, INOUT paramInt INTEGER, INOUT paramBigInt BIGINT, INOUT paramFloat
  FLOAT(2), INOUT paramDouble DOUBLE PRECISION, INOUT paramBool BOOLEAN, INOUT paramString VARCHAR(50),
  INOUT paramNumeric NUMERIC(10,3), INOUT paramDecimal DECIMAL(10,3), INOUT paramReal REAL, INOUT paramTinyInt
  SMALLINT, INOUT paramSmallInt SMALLINT, INOUT paramClob TEXT, INOUT paramBinary BYTEA) AS '
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
' LANGUAGE plpgsql;
/
CREATE TABLE IF NOT EXISTS ArrayTypes(
  row_id        INTEGER,
  int_array     INTEGER ARRAY,
  long_array    BIGINT ARRAY,
  float_array   FLOAT ARRAY,
  double_array  DOUBLE PRECISION ARRAY,
  boolean_array BOOLEAN ARRAY,
  string_array  VARCHAR(50) ARRAY,
  PRIMARY KEY (row_id)
);
/
INSERT INTO ArrayTypes (row_id, int_array, long_array, float_array, double_array, boolean_array, string_array)
  VALUES (1, ARRAY[1, 2, 3], ARRAY [100000000, 200000000, 300000000], ARRAY [245.23, 5559.49, 8796.123],
  ARRAY [245.23, 5559.49, 8796.123], ARRAY [TRUE, FALSE, TRUE], ARRAY ['Hello', 'Ballerina']);
/
CREATE FUNCTION TestArrayOutParams (OUT intArray INTEGER ARRAY, OUT longArray BIGINT ARRAY, OUT floatArray FLOAT ARRAY,
  OUT doubleArray DOUBLE PRECISION ARRAY, OUT boolArray BOOLEAN ARRAY, OUT varcharArray VARCHAR(50) ARRAY) AS '
  BEGIN
  SELECT int_array INTO intArray FROM ArrayTypes where row_id = 1;
  SELECT long_array INTO longArray FROM ArrayTypes where row_id = 1;
  SELECT float_array INTO floatArray FROM ArrayTypes where row_id = 1;
  SELECT double_array INTO doubleArray FROM ArrayTypes where row_id = 1;
  SELECT boolean_array INTO boolArray FROM ArrayTypes where row_id = 1;
  SELECT string_array INTO varcharArray FROM ArrayTypes where row_id = 1;
  END
' LANGUAGE plpgsql;
/
CREATE FUNCTION TestDateTimeOutParams (IN id BIGINT, IN dateVal DATE, IN timeVal TIME, IN datetimeVal TIMESTAMP,
  IN timestampVal TIMESTAMPTZ, OUT dateValOUT DATE, OUT timeValOUT TIME, OUT datetmOut TIMESTAMP, OUT timestOut
  TIMESTAMPTZ) AS '
  BEGIN
  INSERT INTO DateTimeTypes (row_id, date_type, time_type, datetime_type, timestamp_type) VALUES
  (id, dateVal, timeVal, datetimeVal, timestampVal);
  SELECT date_type INTO dateValOUT FROM DateTimeTypes where row_id = id;
  SELECT time_type INTO timeValOUT FROM DateTimeTypes where row_id = id;
  SELECT datetime_type INTO datetmOut FROM DateTimeTypes where row_id = id;
  SELECT timestamp_type INTO timestOut FROM DateTimeTypes where row_id = id;
  END
' LANGUAGE plpgsql;
/
CREATE FUNCTION TestDateINOUTParams (IN id BIGINT, INOUT dateVal DATE, INOUT timeVal TIME, INOUT datetimeVal
TIMESTAMP, INOUT timestampVal TIMESTAMPTZ) AS '
  BEGIN
  INSERT INTO DateTimeTypes (row_id, date_type, time_type, datetime_type, timestamp_type) VALUES
  (id, dateVal, timeVal, datetimeVal, timestampVal);

  SELECT date_type INTO dateVal FROM DateTimeTypes where row_id = id;
  SELECT time_type INTO timeVal FROM DateTimeTypes where row_id = id;
  SELECT datetime_type INTO datetimeVal FROM DateTimeTypes where row_id = id;
  SELECT timestamp_type INTO timestampVal FROM DateTimeTypes where row_id = id;
  END
' LANGUAGE plpgsql;
/
CREATE FUNCTION TestArrayINOutParams (IN id INTEGER, OUT insertedCount INTEGER, INOUT intArray BIGINT ARRAY,
  INOUT longArray BIGINT ARRAY, INOUT floatArray FLOAT ARRAY, INOUT doubleArray DOUBLE PRECISION ARRAY,
  INOUT boolArray BOOLEAN ARRAY, INOUT varcharArray VARCHAR(50) ARRAY) AS '
  BEGIN
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
' LANGUAGE plpgsql;
/
CREATE TYPE customtype AS (val INTEGER);
/
CREATE TABLE structdatatable(id INTEGER, structdata customtype);
/
INSERT INTO structdatatable(id,structdata) VALUES (1,ROW(10)::customtype);
/
CREATE FUNCTION TestStructOut (OUT var customtype) AS '
  BEGIN
  SELECT structdata INTO var from structdatatable where id = 1;
  END
' LANGUAGE plpgsql;
/
CREATE FUNCTION TestStructInOut (OUT countVal INTEGER, INOUT var customtype) AS '
  BEGIN
  INSERT INTO structdatatable(id,structdata) VALUES (2,var);
  select count(*) into countVal from structdatatable where id = 2;
 SELECT structdata INTO var from structdatatable where id = 1;
  END
' LANGUAGE plpgsql;
/
CREATE TABLE employeeItr (id INTEGER NOT NULL, name VARCHAR(20), address VARCHAR(20));
/
INSERT INTO employeeItr VALUES (1, 'Manuri', 'Sri Lanka');
/
INSERT INTO employeeItr VALUES (2, 'Devni', 'Sri Lanka');
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
CREATE TABLE IF NOT EXISTS BlobTable(
  row_id       INTEGER,
  blob_type    OID
);
/
INSERT INTO BlobTable VALUES (1, lo_from_bytea(16458, E'\\x77736F322062616C6C6572696E6120626C6F6220746573742E'));
/
