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
INSERT INTO StringTypes(id, varchar_type, charmax_type, char_type, charactermax_type, character_type,
     nvarcharmax_type, longvarchar_type, clob_type)
    VALUES (1, 'test0', 'test1', 'a', 'test2', 'b', 'test3', 'test4', 'hello ballerina');
/
CREATE TABLE NumericTypes (
   id INT,
   int_type INT,
   bigint_type BIGINT,
   smallint_type SMALLINT,
   tinyint_type TINYINT,
   bit_type BIT,
   decimal_type DECIMAL(10,2),
   numeric_type NUMERIC(10,2),
   float_type FLOAT,
   real_type REAL,
   double_type DOUBLE,
   PRIMARY KEY (id)
);
/
INSERT INTO NumericTypes (id, int_type, bigint_type, smallint_type, tinyint_type, bit_type, decimal_type, numeric_type,
    float_type, real_type, double_type) VALUES (1, 2147483647, 9223372036854774807, 32767, 127, 1, 1234.56, 1234.56,
    1234.56, 1234.56, 1234.56);
/
CREATE PROCEDURE InsertStringData(IN p_id INTEGER, IN p_varchar_type VARCHAR(255), p_charmax_type CHAR(10),
    p_char_type CHAR, p_charactermax_type CHARACTER(10), p_character_type CHARACTER, p_nvarcharmax_type NVARCHAR(255),
    p_longvarchar_type LONGVARCHAR, p_clob_type CLOB )
  MODIFIES SQL DATA
  BEGIN ATOMIC
  INSERT INTO StringTypes(id, varchar_type, charmax_type, char_type, charactermax_type, character_type,
     nvarcharmax_type, longvarchar_type, clob_type)
    VALUES (p_id, p_varchar_type, p_charmax_type, p_char_type, p_charactermax_type, p_character_type,
     p_nvarcharmax_type, p_longvarchar_type, p_clob_type);
  END
/
CREATE PROCEDURE SelectStringData()
  READS SQL DATA DYNAMIC RESULT SETS 1
  BEGIN ATOMIC
  DECLARE result CURSOR WITH RETURN FOR SELECT varchar_type, charmax_type, char_type, charactermax_type,
    character_type, nvarcharmax_type, longvarchar_type, clob_type FROM StringTypes where id = 1 FOR READ ONLY;
  open result;
  END
/
CREATE PROCEDURE SelectStringDataMultiple()
  READS SQL DATA DYNAMIC RESULT SETS 2
  BEGIN ATOMIC
  DECLARE result1 CURSOR WITH RETURN FOR SELECT varchar_type, charmax_type, char_type, charactermax_type,
    character_type, nvarcharmax_type, longvarchar_type, clob_type FROM StringTypes where id = 1 FOR READ ONLY;
  DECLARE result2 CURSOR WITH RETURN FOR SELECT varchar_type FROM StringTypes where id = 1 FOR READ ONLY;
  open result1;
  open result2;
  END
/
CREATE PROCEDURE SelectStringDataWithOutParams (IN p_id INT, OUT p_varchar_type VARCHAR(255),
    OUT p_charmax_type CHAR(10), OUT p_char_type CHAR, OUT p_charactermax_type CHARACTER(10),
    OUT p_character_type CHARACTER, OUT p_nvarcharmax_type NVARCHAR(255), OUT p_longvarchar_type LONGVARCHAR,
    OUT p_clob_type CLOB)
  READS SQL DATA
  BEGIN ATOMIC
  SELECT varchar_type INTO p_varchar_type FROM StringTypes where id = p_id;
  SELECT charmax_type INTO p_charmax_type FROM StringTypes where id = p_id;
  SELECT char_type INTO p_char_type FROM StringTypes where id = p_id;
  SELECT charactermax_type INTO p_charactermax_type FROM StringTypes where id = p_id;
  SELECT character_type INTO p_character_type FROM StringTypes where id = p_id;
  SELECT nvarcharmax_type INTO p_nvarcharmax_type FROM StringTypes where id = p_id;
  SELECT longvarchar_type INTO p_longvarchar_type FROM StringTypes where id = p_id;
  SELECT clob_type INTO p_clob_type FROM StringTypes where id = p_id;
  END
/
CREATE PROCEDURE SelectNumericDataWithOutParams (IN p_id INT, OUT p_int_type INT,OUT p_bigint_type BIGINT,
    OUT p_smallint_type SMALLINT, OUT p_tinyint_type TINYINT,OUT p_bit_type BIT, OUT p_decimal_type DECIMAL(10,2),
    OUT p_numeric_type NUMERIC(10,2), OUT p_float_type FLOAT, OUT p_real_type REAL, OUT p_double_type DOUBLE)
  READS SQL DATA
  BEGIN ATOMIC
  SELECT int_type INTO p_int_type FROM NumericTypes where id = p_id;
  SELECT bigint_type INTO p_bigint_type FROM NumericTypes where id = p_id;
  SELECT smallint_type INTO p_smallint_type FROM NumericTypes where id = p_id;
  SELECT tinyint_type INTO p_tinyint_type FROM NumericTypes where id = p_id;
  SELECT bit_type INTO p_bit_type FROM NumericTypes where id = p_id;
  SELECT decimal_type INTO p_decimal_type FROM NumericTypes where id = p_id;
  SELECT numeric_type INTO p_numeric_type FROM NumericTypes where id = p_id;
  SELECT float_type INTO p_float_type FROM NumericTypes where id = p_id;
  SELECT real_type INTO p_real_type FROM NumericTypes where id = p_id;
  SELECT double_type INTO p_double_type FROM NumericTypes where id = p_id;
  END
/
