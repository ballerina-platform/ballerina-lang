CREATE TABLE IF NOT EXISTS DataTable(
  row_id       INTEGER,
  int_type     INTEGER,
  long_type    BIGINT,
  float_type   FLOAT,
  double_type  DOUBLE,
  boolean_type BOOLEAN,
  string_type  VARCHAR(50),
  decimal_type DECIMAL(20, 2),
  PRIMARY KEY (row_id)
);
/
INSERT INTO DataTable (row_id, int_type, long_type, float_type, double_type, boolean_type, string_type, decimal_type)
  VALUES(1, 1, 9223372036854774807, 123.34, 2139095039, TRUE, 'Hello', 23.45);
/
INSERT INTO DataTable (row_id) VALUES (2);
/
CREATE TABLE IF NOT EXISTS DataTableRep(
  row_id       INTEGER,
  int_type     INTEGER,
  PRIMARY KEY (row_id)
);
/
INSERT INTO DataTableRep (row_id, int_type) VALUES (1, 100);
/
INSERT INTO DataTableRep (row_id, int_type) VALUES (2, 200);
/
CREATE TABLE IF NOT EXISTS FloatTable(
  row_id       INTEGER,
  float_type   FLOAT,
  double_type  DOUBLE,
  numeric_type NUMERIC(10,2),
  decimal_type  DECIMAL(10,2),
  PRIMARY KEY (row_id)
);
/
INSERT INTO FloatTable (row_id, float_type, double_type, numeric_type, decimal_type) VALUES
  (1, 238999.34, 238999.34, 238999.34, 238999.34);
/
CREATE TABLE IF NOT EXISTS ComplexTypes(
  row_id         INTEGER NOT NULL,
  blob_type      BLOB(1024),
  clob_type      CLOB(1024),
  binary_type  BINARY(27),
  PRIMARY KEY (row_id)
);
/
INSERT INTO ComplexTypes (row_id, blob_type, clob_type, binary_type) VALUES
  (1, X'77736F322062616C6C6572696E6120626C6F6220746573742E', CONVERT('very long text', CLOB),
  X'77736F322062616C6C6572696E612062696E61727920746573742E');
/
INSERT INTO ComplexTypes (row_id, blob_type, clob_type, binary_type) VALUES
  (2, null, null, null);
/
CREATE TABLE IF NOT EXISTS ArrayTypes(
  row_id        INTEGER NOT NULL,
  int_array     ARRAY,
  long_array    ARRAY,
  float_array   ARRAY,
  double_array  ARRAY,
  boolean_array ARRAY,
  string_array  ARRAY,
  PRIMARY KEY (row_id)
);
/
INSERT INTO ArrayTypes (row_id, int_array, long_array, float_array, double_array, boolean_array, string_array)
  VALUES (1, (1, 2, 3), (100000000, 200000000, 300000000), (245.23, 5559.49, 8796.123),
  (245.23, 5559.49, 8796.123), (TRUE, FALSE, TRUE), ('Hello', 'Ballerina'));
/
INSERT INTO ArrayTypes (row_id, int_array, long_array, float_array, double_array, boolean_array, string_array)
  VALUES (2, (NULL, 2, 3), (100000000, NULL, 300000000), (NULL, 5559.49, NULL),
  (NULL, NULL, 8796.123), (NULL , NULL, TRUE), (NULL, 'Ballerina'));
/
INSERT INTO ArrayTypes (row_id, int_array, long_array, float_array, double_array, boolean_array, string_array)
  VALUES (3, NULL, NULL, NULL, NULL, NULL, NULL);
/
INSERT INTO ArrayTypes (row_id, int_array, long_array, float_array, double_array, boolean_array, string_array)
  VALUES (5, (NULL, NULL, NULL), (NULL, NULL, NULL), (NULL, NULL, NULL),
  (NULL, NULL, NULL), (NULL , NULL, NULL), (NULL, NULL));
/
CREATE TABLE IF NOT EXISTS MixTypes (
  row_id INTEGER NOT NULL,
  int_type INTEGER,
  long_type BIGINT,
  float_type FLOAT,
  double_type DOUBLE,
  boolean_type BOOLEAN,
  string_type VARCHAR (50),
  decimal_type decimal,
  int_array ARRAY,
  long_array ARRAY,
  float_array ARRAY,
  double_array ARRAY,
  boolean_array ARRAY,
  string_array ARRAY,
  PRIMARY KEY (row_id)
);
/
INSERT INTO MixTypes (row_id, int_type, long_type, float_type, double_type, boolean_type, string_type, decimal_type,
  int_array, long_array, float_array, double_array, boolean_array, string_array)
VALUES (1, 1, 9223372036854774807, 123.34, 2139095039, TRUE, 'Hello', 342452151425.4556, (1, 2, 3),
  (100000000, 200000000, 300000000), (245.23, 5559.49, 8796.123),
  (245.23, 5559.49, 8796.123), (TRUE, FALSE, TRUE), ('Hello', 'Ballerina'));
/
CREATE TABLE IF NOT EXISTS DateTimeTypes(
  row_id         INTEGER NOT NULL,
  date_type      DATE,
  time_type      TIME,
  timestamp_type timestamp,
  datetime_type  datetime,
  PRIMARY KEY (row_id)
);
/
CREATE TABLE IF NOT EXISTS IntegerTypes (
  id INTEGER,
  intData INTEGER,
  tinyIntData TINYINT,
  smallIntData SMALLINT,
  bigIntData BIGINT
);
/
CREATE TABLE IF NOT EXISTS DataTypeTableNillable(
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
  date_type      DATE,
  time_type      TIME,
  datetime_type  DATETIME,
  timestamp_type TIMESTAMP,
  PRIMARY KEY (row_id)
);
/
CREATE TABLE IF NOT EXISTS DataTypeTableNillableBlob(
  row_id       INTEGER,
  blob_type    BLOB,
  PRIMARY KEY (row_id)
);
/
INSERT INTO DataTypeTableNillable (row_id, int_type, long_type, float_type, double_type, boolean_type, string_type,
  numeric_type, decimal_type, real_type, tinyint_type, smallint_type, clob_type, blob_type, binary_type, date_type,
  time_type, datetime_type, timestamp_type) VALUES
  (1, 10, 9223372036854774807, 123.34, 2139095039, TRUE, 'Hello',1234.567, 1234.567, 1234.567, 1, 5555,
  CONVERT('very long text', CLOB), X'77736F322062616C6C6572696E6120626C6F6220746573742E',
  X'77736F322062616C6C6572696E612062696E61727920746573742E', '2017-02-03', '11:35:45', '2017-02-03 11:53:00',
  '2017-02-03 11:53:00');
/
INSERT INTO DataTypeTableNillable (row_id, int_type, long_type, float_type, double_type, boolean_type, string_type,
  numeric_type, decimal_type, real_type, tinyint_type, smallint_type, clob_type, blob_type, binary_type, date_type,
  time_type, datetime_type, timestamp_type) VALUES
  (2, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
/
INSERT INTO DataTypeTableNillableBlob (row_id, blob_type) VALUES
  (3, X'77736F322062616C6C6572696E6120626C6F6220746573742E');
/
INSERT INTO DataTypeTableNillableBlob (row_id, blob_type) VALUES (4, null);
/
CREATE TABLE IF NOT EXISTS Person(
  id       INTEGER,
  age      INTEGER,
  salary   FLOAT,
  name  VARCHAR(50),
  PRIMARY KEY (id)
);
/

INSERT INTO Person (id, age, salary, name) VALUES (1, 25, 400.25, 'John');
/
INSERT INTO Person (id, age, salary, name) VALUES (2, 35, 600.25, 'Anne');
/
INSERT INTO Person (id, age, salary, name) VALUES (3, 45, 600.25, 'Mary');
/
INSERT INTO Person (id, age, salary, name) VALUES (10, 22, 100.25, 'Peter');
/
