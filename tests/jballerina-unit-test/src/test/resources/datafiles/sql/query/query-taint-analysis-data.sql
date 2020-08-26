CREATE TABLE IF NOT EXISTS Person(
  id       INTEGER,
  name     VARCHAR(50),
  age      INTEGER
);
/

INSERT INTO Person (id, name, age) VALUES (1, 'John', 25);
/
INSERT INTO Person (id, name, age) VALUES (2, 'Anne', 35);
/

CREATE TABLE IF NOT EXISTS PartTimeEmployee(
  partTimeEmpId    INTEGER,
  name             VARCHAR(50),
  salary           FLOAT,
  noOfHours        INTEGER
);
/
