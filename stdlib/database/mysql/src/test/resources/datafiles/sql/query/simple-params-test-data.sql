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

INSERT INTO DataTable (row_id, int_type, long_type, float_type, double_type, boolean_type, string_type, decimal_type)
  VALUES(1, 1, 9223372036854774807, 123.34, 2139095039, TRUE, 'Hello', 23.45);

INSERT INTO DataTable (row_id) VALUES (2);

INSERT INTO DataTable (row_id, int_type, long_type, float_type, double_type, boolean_type, string_type, decimal_type)
  VALUES(3, 1, 9372036854774807, 124.34, 29095039, false, '1', 25.45);

CREATE TABLE IF NOT EXISTS ComplexTypes(
  row_id         INTEGER NOT NULL,
  tinyblob_type     TINYBLOB,
  blob_type         BLOB,
  mediumblob_type   MEDIUMBLOB,
  longblob_type     LONGBLOB,
  tinytext_type   TINYTEXT,
  text_type       TEXT,
  mediumtext_type MEDIUMTEXT,
  longtext_type   LONGTEXT,
  binary_type  BINARY(27),
  var_binary_type VARBINARY(27),
  PRIMARY KEY (row_id)
);

INSERT INTO ComplexTypes (row_id, tinyblob_type, blob_type, mediumblob_type, longblob_type, tinytext_type, text_type,
mediumtext_type, longtext_type, binary_type, var_binary_type) VALUES
  (1, X'77736F322062616C6C6572696E6120626C6F6220746573742E', X'77736F322062616C6C6572696E6120626C6F6220746573742E',
  X'77736F322062616C6C6572696E6120626C6F6220746573742E', X'77736F322062616C6C6572696E6120626C6F6220746573742E',
  'very long text', 'very long text','very long text','very long text',
  X'77736F322062616C6C6572696E612062696E61727920746573742E', X'77736F322062616C6C6572696E612062696E61727920746573742E');

INSERT INTO ComplexTypes (row_id, tinyblob_type, blob_type, mediumblob_type, longblob_type, tinytext_type, text_type,
mediumtext_type, longtext_type, binary_type, var_binary_type) VALUES
  (2, null, null, null, null, null, null, null, null, null, null);

CREATE TABLE NumericTypes (
   id INT AUTO_INCREMENT,
   int_type INT NOT NULL,
   bigint_type BIGINT NOT NULL,
   smallint_type SMALLINT NOT NULL ,
   mediumint_type MEDIUMINT NOT NULL ,
   tinyint_type TINYINT NOT NULL ,
   bit_type BIT NOT NULL ,
   decimal_type DECIMAL(10,3) NOT NULL ,
   numeric_type NUMERIC(10,3) NOT NULL ,
   float_type FLOAT NOT NULL ,
   real_type REAL NOT NULL ,
   PRIMARY KEY (id)
);

INSERT INTO NumericTypes (id, int_type, bigint_type, smallint_type, mediumint_type, tinyint_type, bit_type, decimal_type, numeric_type,
    float_type, real_type) VALUES (1, 2147483647, 9223372036854774807, 32767, 8388607, 127, 1, 1234.567, 1234.567, 1234.567,
    1234.567);

INSERT INTO NumericTypes (id, int_type, bigint_type, smallint_type, mediumint_type, tinyint_type, bit_type, decimal_type, numeric_type,
    float_type, real_type) VALUES (2, 2147483647, 9223372036854774807, 32767, 8388607, 127, 1, 1234, 1234, 1234,
    1234);

CREATE TABLE IF NOT EXISTS DateTimeTypes(
  row_id         INTEGER NOT NULL,
  date_type      DATE,
  time_type      TIME,
  timestamp_type TIMESTAMP,
  datetime_type  DATETIME,
  PRIMARY KEY (row_id)
);

INSERT INTO DateTimeTypes (row_id, date_type, time_type, datetime_type, timestamp_type) VALUES
  (1,'2017-02-03', '11:35:45', '2017-02-03 11:53:00', '2017-02-03 11:53:00');

CREATE TABLE ENUMTable (
    id integer NOT NULL,
    enum_type ENUM('admin','doctor','housekeeper') DEFAULT NULL,
    PRIMARY KEY (id)
);

INSERT INTO ENUMTable(id, enum_type) VALUES (1, 'doctor');

CREATE TABLE SetTable (
    row_id INTEGER NOT NULL,
    set_type SET('a', 'b', 'c', 'd')
);

INSERT INTO SetTable (row_id, set_type) VALUES (1, 'a,d'), (2, 'd,a'), (3, 'a,d,a');

CREATE TABLE GEOTable(
    id INTEGER NOT NULL ,
    geom GEOMETRY
);

INSERT INTO GEOTable (id, geom) values (1, ST_GeomFromText('POINT(7 52)'));

CREATE TABLE JsonTable(
    id INTEGER NOT NULL ,
    json_type JSON
);

INSERT INTO JsonTable (id, json_type) values (1, JSON_OBJECT('id', 100, 'name', 'Joe', 'groups', '[2,5]'));