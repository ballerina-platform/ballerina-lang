CREATE TABLE IF NOT EXISTS DataTable(
  id INT IDENTITY,
  int_type     INTEGER UNIQUE,
  long_type    BIGINT,
  float_type   FLOAT,
  PRIMARY KEY (id)
);
/
INSERT INTO DataTable (int_type, long_type, float_type)
  VALUES(1, 9223372036854774807, 123.34);
/

INSERT INTO DataTable (int_type, long_type, float_type)
  VALUES(2, 9372036854774807, 124.34);
/
