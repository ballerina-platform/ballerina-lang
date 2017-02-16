CREATE TABLE IF NOT EXISTS DataTable(
  row_id       INTEGER NOT NULL IDENTITY,
  int_type     INTEGER,
  long_type    BIGINT,
  float_type   FLOAT,
  double_type  DOUBLE,
  boolean_type BOOLEAN,
  string_type  VARCHAR(50),
  PRIMARY KEY (row_id)
);
/
insert into DataTable (int_type, long_type, float_type, double_type, boolean_type, string_type) values
  (1, 9223372036854774807, 123.34, 2139095039, TRUE, 'Hello');
/
CREATE TABLE IF NOT EXISTS ComplexTypes(
  row_id         INTEGER NOT NULL IDENTITY,
  blob_type      BLOB(1024),
  clob_type      CLOB(1024),
  time_type      TIME,
  date_type      DATE,
  timestamp_type timestamp,
  PRIMARY KEY (row_id)
);
/
insert into ComplexTypes (blob_type, clob_type, time_type, date_type, timestamp_type) values
  (X'77736F322062616C6C6572696E6120626C6F6220746573742E', CONVERT('very long text', CLOB), '11:35:45', '2017-02-03',
   '2017-02-03 11:53:00');
/
CREATE TABLE IF NOT EXISTS ArrayTypes(
  row_id        INTEGER NOT NULL IDENTITY,
  int_array     INTEGER ARRAY,
  long_array    BIGINT ARRAY,
  float_array   FLOAT ARRAY,
  double_array  DOUBLE ARRAY,
  boolean_array BOOLEAN ARRAY,
  string_array  VARCHAR(50) ARRAY,
  PRIMARY KEY (row_id)
);
/
INSERT INTO ArrayTypes (int_array, long_array, float_array, double_array, boolean_array, string_array)
VALUES ARRAY[1, 2, 3], ARRAY [100000000, 200000000, 300000000], ARRAY [245.23, 5559.49, 8796.123],
ARRAY [245.23, 5559.49, 8796.123], ARRAY [TRUE, FALSE, TRUE], ARRAY ['Hello', 'Ballerina'];
/

CREATE TABLE IF NOT EXISTS MixTypes (
  row_id INTEGER NOT NULL IDENTITY,
  int_type INTEGER,
  long_type BIGINT,
  float_type FLOAT,
  double_type DOUBLE,
  boolean_type BOOLEAN,
  string_type VARCHAR (50),
  int_array INTEGER ARRAY,
  long_array BIGINT ARRAY,
  float_array FLOAT ARRAY,
  double_array DOUBLE ARRAY,
  boolean_array BOOLEAN ARRAY,
  string_array VARCHAR (50
) ARRAY,
PRIMARY KEY (row_id
)
);
/
INSERT INTO MixTypes (int_type, long_type, float_type, double_type, boolean_type, string_type, int_array, long_array, float_array, double_array, boolean_array, string_array)
VALUES 1, 9223372036854774807, 123.34, 2139095039, TRUE, 'Hello', ARRAY [1, 2, 3], ARRAY [100000000, 200000000, 300000000], ARRAY [245.23, 5559.49, 8796.123],
  ARRAY [245.23, 5559.49, 8796.123], ARRAY [TRUE, FALSE, TRUE], ARRAY ['Hello', 'Ballerina'];
/