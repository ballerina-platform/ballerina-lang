DROP DATABASE IF EXISTS StreamTestDB;
CREATE DATABASE StreamTestDB;
GRANT all on StreamTestDB.* to 'user1'@'localhost' identified by 'pass1';
USE StreamTestDB;
CREATE TABLE Data (id INT, field1 VARCHAR(1024), field2 VARCHAR(1024));

DELIMITER $$
 DROP PROCEDURE IF EXISTS PopulateData$$
 CREATE PROCEDURE PopulateData(IN row_count INT)
       BEGIN
               DECLARE count INT;
               DECLARE strDataEntry VARCHAR(1024);
               SET count = 1;
               SET strDataEntry =  '';
               WHILE count <= 1024 DO
                           SET strDataEntry = CONCAT(strDataEntry, 'x');
                           SET count = count + 1;
               END WHILE;
               SET count = 1;
               WHILE count <= row_count DO
                           INSERT INTO Data VALUES (count, strDataEntry, strDataEntry);
                           SET count = count + 1;
               END WHILE;
               SELECT strDataEntry;
       END$$
   DELIMITER ;

CALL PopulateData(100000);
