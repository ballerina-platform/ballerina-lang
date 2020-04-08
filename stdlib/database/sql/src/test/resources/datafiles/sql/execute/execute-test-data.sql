CREATE TABLE NumericTypes (
   id INT IDENTITY,
   int_type INT,
   bigint_type BIGINT,
   smallint_type SMALLINT,
   tinyint_type TINYINT,
   bit_type BIT,
   decimal_type DECIMAL(10,2),
   numeric_type NUMERIC(10,2),
   float_type FLOAT,
   real_type REAL,
   PRIMARY KEY (id)
);
/
INSERT INTO NumericTypes (int_type) VALUES (10);
/
CREATE TABLE StringTypes (
   id INT,
   varchar_type VARCHAR(255),
   charmax_type CHAR(10),
   char_type CHAR,
   charactermax_type CHARACTER(10),
   character_type CHARACTER,
   nvarcharmax_type NVARCHAR(255),
   longvarchar_type LONGVARCHAR,
   clob_type CLOB,
   PRIMARY KEY (id)
);
/
