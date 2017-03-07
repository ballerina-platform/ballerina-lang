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
insert into DataTypeTable (row_id,int_type, long_type, float_type, double_type, boolean_type, string_type,numeric_type,
  decimal_type,real_type,tinyint_type,smallint_type,clob_type,blob_type,binary_type) values
  (1, 10, 9223372036854774807, 123.34, 2139095039, TRUE, 'Hello',1234.567,1234.567,1234.567,1,5555,
  CONVERT('very long text', CLOB),X'77736F322062616C6C6572696E6120626C6F6220746573742E',
  X'77736F322062616C6C6572696E612062696E61727920746573742E');
/
insert into Customers (firstName,lastName,registrationID,creditLimit,country)
  values ('Peter', 'Stuart', 1, 5000.75, 'USA');
/
CREATE PROCEDURE InsertPersonData(IN p_RegID INTEGER, IN p_PersonName VARCHAR(50))
  MODIFIES SQL DATA
  BEGIN ATOMIC
  INSERT INTO Customers(registrationID, firstName,lastName, creditLimit,country)
    VALUES (p_RegID, p_PersonName, p_PersonName, 25000, 'UK');
  END
/
CREATE PROCEDURE GetCustomerID (IN regID INT,OUT credLimit DOUBLE)
  READS SQL DATA
  BEGIN ATOMIC
  SELECT creditLimit INTO credLimit FROM Customers where registrationID = regID ;
  END
/
CREATE PROCEDURE GetCustomerCountry (IN regID INT,INOUT param VARCHAR(300))
  READS SQL DATA
  BEGIN ATOMIC
  SELECT country INTO param FROM Customers where lastName = param and registrationID = regID;
  END
/
CREATE PROCEDURE TestOutParams (IN id INT,OUT paramInt INT,OUT paramBigInt BIGINT,OUT paramFloat FLOAT,
  OUT paramDouble DOUBLE,OUT paramBool BOOLEAN,OUT paramString VARCHAR(50),
  OUT paramNumeric NUMERIC,OUT paramDecimal DECIMAL,OUT paramReal REAL,OUT paramTinyInt TINYINT,
  OUT paramSmallInt SMALLINT,OUT paramClob CLOB,OUT paramBlob BLOB, OUT paramBinary BINARY(27))
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