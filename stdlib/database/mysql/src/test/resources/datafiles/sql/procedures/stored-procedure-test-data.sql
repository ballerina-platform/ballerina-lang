CREATE TABLE StringTypes (
                             id INT,
                             varchar_type VARCHAR(255),
                             charmax_type CHAR(10),
                             char_type CHAR,
                             charactermax_type CHARACTER(10),
                             character_type CHARACTER,
                             nvarcharmax_type NVARCHAR(255),
                             PRIMARY KEY (id)
);

INSERT INTO StringTypes(id, varchar_type, charmax_type, char_type, charactermax_type, character_type, nvarcharmax_type)
                    VALUES (1, 'test0', 'test1', 'a', 'test2', 'b', 'test3');

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

INSERT INTO NumericTypes (id, int_type, bigint_type, smallint_type, tinyint_type, bit_type, decimal_type, numeric_type,
                          float_type, real_type, double_type)
                  VALUES (1, 2147483647, 9223372036854774807, 32767, 127, 1, 1234.56, 1234.56,
                                                                      1234.56, 1234.56, 1234.56);


DELIMITER $
CREATE PROCEDURE InsertStringData(IN p_id INTEGER,
                                  IN p_varchar_type VARCHAR(255),
                                  IN p_charmax_type CHAR(10),
                                  IN p_char_type CHAR,
                                  IN p_charactermax_type CHARACTER(10),
                                  IN p_character_type CHARACTER,
                                  IN p_nvarcharmax_type NVARCHAR(255))
Begin
INSERT INTO StringTypes(id, varchar_type, charmax_type, char_type, charactermax_type, character_type, nvarcharmax_type)
VALUES (p_id, p_varchar_type, p_charmax_type, p_char_type, p_charactermax_type, p_character_type, p_nvarcharmax_type);
End$

DELIMITER $
CREATE PROCEDURE SelectStringData()
Begin
SELECT varchar_type, charmax_type, char_type, charactermax_type, character_type, nvarcharmax_type
       FROM StringTypes
where id = 1;
END$

DELIMITER $
CREATE PROCEDURE SelectStringDataMultiple()
Begin
SELECT varchar_type, charmax_type, char_type, charactermax_type, character_type, nvarcharmax_type FROM StringTypes where id = 1;
SELECT varchar_type FROM StringTypes where id = 1;
END$

DELIMITER $
CREATE PROCEDURE SelectStringDataWithOutParams (IN p_id INT, OUT p_varchar_type VARCHAR(255),
                                                OUT p_charmax_type CHAR(10), OUT p_char_type CHAR, OUT p_charactermax_type CHARACTER(10),
                                                OUT p_character_type CHARACTER, OUT p_nvarcharmax_type NVARCHAR(255))
Begin
SELECT varchar_type INTO p_varchar_type FROM StringTypes where id = p_id;
SELECT charmax_type INTO p_charmax_type FROM StringTypes where id = p_id;
SELECT char_type INTO p_char_type FROM StringTypes where id = p_id;
SELECT charactermax_type INTO p_charactermax_type FROM StringTypes where id = p_id;
SELECT character_type INTO p_character_type FROM StringTypes where id = p_id;
SELECT nvarcharmax_type INTO p_nvarcharmax_type FROM StringTypes where id = p_id;
END$

DELIMITER $
CREATE PROCEDURE SelectNumericDataWithOutParams (IN p_id INT, OUT p_int_type INT,OUT p_bigint_type BIGINT,
                                                 OUT p_smallint_type SMALLINT, OUT p_tinyint_type TINYINT,OUT p_bit_type BIT, OUT p_decimal_type DECIMAL(10,2),
                                                 OUT p_numeric_type NUMERIC(10,2), OUT p_float_type FLOAT, OUT p_real_type REAL, OUT p_double_type DOUBLE)
Begin
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
END$
