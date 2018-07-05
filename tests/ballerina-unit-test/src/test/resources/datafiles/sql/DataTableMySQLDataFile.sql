CREATE TABLE IF NOT EXISTS DataTable(
  row_id       INT,
  int_type     INT,
  long_type    BIGINT,
  float_type   FLOAT,
  double_type  DOUBLE,
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
  row_id       INT,
  int_type     INT,
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
  row_id       INT,
  float_type   FLOAT(8, 2),
  double_type  DOUBLE,
  numeric_type NUMERIC(10,2),
  decimal_type  DECIMAL(10,2),
  PRIMARY KEY (row_id)
);
/
insert into FloatTable (row_id, float_type, double_type, numeric_type, decimal_type) values
  (1, 238999.34, 238999.34, 238999.34, 238999.34);
/
CREATE TABLE IF NOT EXISTS ComplexTypes(
  row_id         INT NOT NULL,
  blob_type      BLOB(1024),
  clob_type      TEXT,
  binary_type  BINARY(27),
  PRIMARY KEY (row_id)
);
/
insert into ComplexTypes (row_id, blob_type, clob_type, binary_type) values
  (1, X'77736F322062616C6C6572696E6120626C6F6220746573742E', 'very long text',
  X'77736F322062616C6C6572696E612062696E61727920746573742E');
/
CREATE TABLE IF NOT EXISTS DateTimeTypes(
  row_id         INT NOT NULL,
  date_type      DATE,
  time_type      TIME,
  timestamp_type TIMESTAMP,
  datetime_type  DATETIME(3),
  PRIMARY KEY (row_id)
);
/
CREATE TABLE IF NOT EXISTS IntegerTypes (
  id INT,
  intData INT,
  tinyIntData TINYINT,
  smallIntData SMALLINT,
  bigIntData BIGINT
);
/
CREATE TABLE IF NOT EXISTS DataTypeTableNillable(
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
  blob_type    BLOB,
  binary_type  BINARY(27),
  date_type      DATE,
  time_type      TIME,
  datetime_type  DATETIME NULL,
  timestamp_type TIMESTAMP NULL,
  PRIMARY KEY (row_id)
);
/
insert into DataTypeTableNillable (row_id, int_type, long_type, float_type, double_type, boolean_type, string_type,
  numeric_type, decimal_type, real_type, tinyint_type, smallint_type, clob_type, blob_type, binary_type, date_type,
  time_type, datetime_type, timestamp_type) values
  (1, 10, 9223372036854774807, 123.34, 2139095039, TRUE, 'Hello', 1234.567, 1234.567, 1234.567, 1, 5555,
  'very long text', X'77736F322062616C6C6572696E6120626C6F6220746573742E',
  X'77736F322062616C6C6572696E612062696E61727920746573742E', '2017-02-03', '11:35:45', '2017-02-03 11:53:00',
  '2017-02-03 11:53:00');
/
insert into DataTypeTableNillable (row_id, int_type, long_type, float_type, double_type, boolean_type, string_type,
  numeric_type, decimal_type, real_type, tinyint_type, smallint_type, clob_type, blob_type, binary_type, date_type,
  time_type, datetime_type, timestamp_type) values
  (2, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
/
