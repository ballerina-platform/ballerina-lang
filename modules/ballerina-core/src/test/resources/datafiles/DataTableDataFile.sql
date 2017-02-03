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