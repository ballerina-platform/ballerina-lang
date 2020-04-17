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
INSERT INTO DataTable (row_id, int_type, long_type, float_type, double_type, boolean_type, string_type, decimal_type)
  VALUES(3, 1, 9372036854774807, 124.34, 29095039, false, '1', 25.45);
/
CREATE TABLE IF NOT EXISTS ComplexTypes(
  row_id         INTEGER NOT NULL,
  blob_type      BLOB(1024),
  clob_type      CLOB(1024),
  binary_type  BINARY(27),
  var_binary_type VARBINARY(27),
  PRIMARY KEY (row_id)
);
/
INSERT INTO ComplexTypes (row_id, blob_type, clob_type, binary_type, var_binary_type) VALUES
  (1, X'77736F322062616C6C6572696E6120626C6F6220746573742E', CONVERT('very long text', CLOB),
  X'77736F322062616C6C6572696E612062696E61727920746573742E', X'77736F322062616C6C6572696E612062696E61727920746573742E');
/
INSERT INTO ComplexTypes (row_id, blob_type, clob_type, binary_type, var_binary_type) VALUES
  (2, null, null, null, null);
/

CREATE TABLE NumericTypes (
   id INT IDENTITY,
   int_type INT NOT NULL,
   bigint_type BIGINT NOT NULL,
   smallint_type SMALLINT NOT NULL ,
   tinyint_type TINYINT NOT NULL ,
   bit_type BIT NOT NULL ,
   decimal_type DECIMAL(10,3) NOT NULL ,
   numeric_type NUMERIC(10,3) NOT NULL ,
   float_type FLOAT NOT NULL ,
   real_type REAL NOT NULL ,
   PRIMARY KEY (id)
);
/
INSERT INTO NumericTypes (id, int_type, bigint_type, smallint_type, tinyint_type, bit_type, decimal_type, numeric_type,
    float_type, real_type) VALUES (1, 2147483647, 9223372036854774807, 32767, 127, 1, 1234.567, 1234.567, 1234.567,
    1234.567);
/

INSERT INTO NumericTypes (id, int_type, bigint_type, smallint_type, tinyint_type, bit_type, decimal_type, numeric_type,
    float_type, real_type) VALUES (2, 2147483647, 9223372036854774807, 32767, 127, 1, 1234, 1234, 1234,
    1234);
/
CREATE TABLE IF NOT EXISTS DateTimeTypes(
  row_id         INTEGER NOT NULL,
  date_type      DATE,
  time_type      TIME,
  timestamp_type TIMESTAMP,
  datetime_type  datetime,
  time_type2      TIME(6) WITH TIME ZONE,
  timestamp_type2 TIMESTAMP(2) WITH TIME ZONE,
  PRIMARY KEY (row_id)
);
/
INSERT INTO DateTimeTypes (row_id, date_type, time_type, datetime_type, timestamp_type, time_type2, timestamp_type2) VALUES
  (1,'2017-02-03', '11:35:45', '2017-02-03 11:53:00', '2017-02-03 11:53:00','20:08:08-8:00','2008-08-08 20:08:08+8:00');
/
CREATE TABLE IF NOT EXISTS ArrayTypes(
  row_id        INTEGER NOT NULL,
  int_array     INTEGER ARRAY,
  long_array    BIGINT ARRAY,
  float_array   FLOAT ARRAY,
  double_array  DOUBLE ARRAY,
  decimal_array  DECIMAL ARRAY,
  boolean_array BOOLEAN ARRAY,
  string_array  VARCHAR(20) ARRAY,
  blob_array    VARBINARY(27) ARRAY,
  PRIMARY KEY (row_id)
);
/
INSERT INTO ArrayTypes (row_id, int_array, long_array, float_array, double_array, decimal_array, boolean_array, string_array, blob_array)
  VALUES (1, ARRAY [1, 2, 3], ARRAY [100000000, 200000000, 300000000], ARRAY[245.23, 5559.49, 8796.123],
  ARRAY[245.23, 5559.49, 8796.123], ARRAY[245, 5559, 8796], ARRAY[TRUE, FALSE, TRUE], ARRAY['Hello', 'Ballerina'],
  ARRAY[X'77736F322062616C6C6572696E6120626C6F6220746573742E']);
/
INSERT INTO ArrayTypes (row_id, int_array, long_array, float_array, double_array,  decimal_array, boolean_array, string_array, blob_array)
  VALUES (2, ARRAY[NULL, 2, 3], ARRAY[100000000, NULL, 300000000], ARRAY[NULL, 5559.49, NULL],
  ARRAY[NULL, NULL, 8796.123], ARRAY[NULL, NULL, 8796], ARRAY[NULL , NULL, TRUE], ARRAY[NULL, 'Ballerina'],
  ARRAY[NULL, X'77736F322062616C6C6572696E6120626C6F6220746573742E']);
/
INSERT INTO ArrayTypes (row_id, int_array, long_array, float_array, double_array, decimal_array, boolean_array, string_array, blob_array)
  VALUES (3, NULL, NULL, NULL, NULL,NULL , NULL, NULL, NULL);
/
INSERT INTO ArrayTypes (row_id, int_array, long_array, float_array, double_array, decimal_array, boolean_array, string_array, blob_array)
  VALUES (5, ARRAY[NULL, NULL, NULL], ARRAY[NULL, NULL, NULL], ARRAY[NULL, NULL, NULL],
  ARRAY[NULL, NULL, NULL], ARRAY[NULL , NULL, NULL], ARRAY[NULL , NULL, NULL], ARRAY[NULL, NULL], ARRAY[NULL, NULL]);
/