CREATE TABLE IF NOT EXISTS DataTable(
  row_id INTEGER NOT NULL AUTO_INCREMENT,
  int_type INTEGER,
  long_type BIGINT,
  float_type REAL,
  double_type DOUBLE,
  boolean_type BOOLEAN,
  string_type  VARCHAR(50),
  PRIMARY KEY (row_id)
);

insert into DataTable (int_type, long_type, float_type, double_type, boolean_type, string_type) values
  (1, 9223372036854774807, 123.34, 9223372036854774807.75, TRUE, 'Hello');

CREATE TABLE IF NOT EXISTS ComplexTypes(
  row_id INTEGER NOT NULL AUTO_INCREMENT,
  blob_type blob,
  clob_type clob,
  time_type time,
  date_type date,
  timestamp_type timestamp,
  PRIMARY KEY (row_id)
);

insert into ComplexTypes (blob_type, clob_type, time_type, date_type, timestamp_type) values
  (RAWTOHEX('test value'), 'very long text', '11:35:45', '2017-02-03', parseDateTime('20170203115300','yyyyMMddHHmmss'));

CREATE TABLE IF NOT EXISTS ArrayTypes(
  row_id INTEGER NOT NULL AUTO_INCREMENT,
  blob_type ARRAY,
  PRIMARY KEY (row_id)
);

insert into ArrayTypes (blob_type) values ((1, 2, 3));