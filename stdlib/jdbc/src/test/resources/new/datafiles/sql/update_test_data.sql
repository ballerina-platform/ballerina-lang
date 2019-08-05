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
   real_type REAL
);
/

insert into NumericTypes (int_type) values (10);
/

CREATE TABLE StringTypes (
   id INT,
   varchar_type varchar(255),
   charmax_type char(10),
   char_type char,
   charactermax_type character(10),
   character_type character,
   nvarcharmax_type nvarchar(255),
   longvarchar_type longvarchar,
   clob_type clob
);
/