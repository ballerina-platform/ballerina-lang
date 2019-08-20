CREATE TABLE IF NOT EXISTS Person(
  id       INTEGER,
  age      INTEGER,
  salary   FLOAT,
  name  VARCHAR(50),
  PRIMARY KEY (id)
);
/

INSERT INTO Person (id, age, salary, name) VALUES (1, 25, 400.25, 'John');
/
INSERT INTO Person (id, age, salary, name) VALUES (2, 35, 600.25, 'Anne');
/
INSERT INTO Person (id, age, salary, name) VALUES (3, 45, 600.25, 'Mary');
/
INSERT INTO Person (id, age, salary, name) VALUES (10, 22, 100.25, 'Peter');
/
