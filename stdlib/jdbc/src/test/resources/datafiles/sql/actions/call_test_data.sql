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
    VALUES (1, 'test0', 'test1', 'a', 'test2', 'b',
     'test3', 'test4', 'hello ballerina');
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
