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