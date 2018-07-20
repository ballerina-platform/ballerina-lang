CREATE TABLE IF NOT EXISTS DataTable(
  row_id       INTEGER,
  int_type     INTEGER,
  long_type    BIGINT,
  float_type   FLOAT(4),
  double_type  DOUBLE PRECISION,
  boolean_type BOOLEAN,
  string_type  VARCHAR(50),
  PRIMARY KEY (row_id)
);
/
insert into DataTable (row_id, int_type, long_type, float_type, double_type, boolean_type, string_type) values
  (1, 1, 9223372036854774807, 123.34, 2139095039, TRUE, 'Hello');
/
insert into DataTable (row_id) values (2);
/
CREATE TABLE IF NOT EXISTS DataTableRep(
  row_id       INTEGER,
  int_type     INTEGER,
  PRIMARY KEY (row_id)
);
/
insert into DataTableRep (row_id, int_type) values
  (1, 100);
/
insert into DataTableRep (row_id, int_type) values
  (2, 200);
/
CREATE TABLE IF NOT EXISTS FloatTable(
  row_id       INTEGER,
  float_type   FLOAT(4),
  double_type  DOUBLE PRECISION,
  numeric_type NUMERIC(10,2),
  decimal_type  DECIMAL(10,2),
  PRIMARY KEY (row_id)
);
/
insert into FloatTable (row_id, float_type, double_type, numeric_type, decimal_type) values
  (1, 238999.34, 238999.34, 238999.34, 238999.34);
/
CREATE TABLE IF NOT EXISTS ArrayTypes(
  row_id        INTEGER NOT NULL,
  int_array     INTEGER ARRAY,
  long_array    BIGINT ARRAY,
  float_array   FLOAT(4) ARRAY,
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
INSERT INTO ArrayTypes (row_id, int_array, long_array, float_array, double_array, boolean_array, string_array)
  VALUES (2, ARRAY[NULL, 2, 3], ARRAY [100000000, NULL, 300000000], ARRAY [NULL, 5559.49, NULL],
  ARRAY [NULL, NULL, 8796.123], ARRAY [NULL , NULL, TRUE], ARRAY [NULL, 'Ballerina']);
/
INSERT INTO ArrayTypes (row_id, int_array, long_array, float_array, double_array, boolean_array, string_array)
  VALUES (3, NULL, NULL, NULL, NULL, NULL, NULL);
/
INSERT INTO ArrayTypes (row_id, int_array, long_array, float_array, double_array, boolean_array, string_array)
  VALUES (5, ARRAY[NULL, NULL, NULL]::INTEGER[], ARRAY [NULL, NULL, NULL]::BIGINT[], ARRAY [NULL, NULL, NULL]::FLOAT[],
  ARRAY [NULL, NULL, NULL]::FLOAT8[], ARRAY [NULL , NULL, NULL]::BOOLEAN[], ARRAY [NULL, NULL]::VARCHAR[]);
/
CREATE TABLE IF NOT EXISTS MixTypes (
  row_id INTEGER NOT NULL,
  int_type INTEGER,
  long_type BIGINT,
  float_type FLOAT(4),
  double_type DOUBLE PRECISION,
  boolean_type BOOLEAN,
  string_type VARCHAR (50),
  int_array INTEGER ARRAY,
  long_array BIGINT ARRAY,
  float_array FLOAT ARRAY,
  double_array DOUBLE PRECISION ARRAY,
  boolean_array BOOLEAN ARRAY,
  string_array VARCHAR (50) ARRAY,
PRIMARY KEY (row_id)
);
/
INSERT INTO MixTypes (row_id, int_type, long_type, float_type, double_type, boolean_type, string_type, int_array, long_array,
  float_array, double_array, boolean_array, string_array)
VALUES (1, 1, 9223372036854774807, 123.34, 2139095039, TRUE, 'Hello', ARRAY [1, 2, 3],
  ARRAY [100000000, 200000000, 300000000], ARRAY [245.23, 5559.49, 8796.123],
  ARRAY [245.23, 5559.49, 8796.123], ARRAY [TRUE, FALSE, TRUE], ARRAY ['Hello', 'Ballerina']);
/
CREATE TABLE IF NOT EXISTS DateTimeTypes(
  row_id         INTEGER NOT NULL,
  date_type      DATE,
  time_type      TIME,
  timestamp_type TIMESTAMPTZ,
  datetime_type  TIMESTAMP,
PRIMARY KEY (row_id)
);
/
CREATE TABLE IF NOT EXISTS IntegerTypes (
  id INTEGER,
  intData INTEGER,
  tinyIntData SMALLINT,
  smallIntData SMALLINT,
  bigIntData BIGINT
);
/

