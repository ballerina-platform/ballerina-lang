CREATE TABLE IF NOT EXISTS Person(
  id       INTEGER,
  age      INTEGER,
  salary   FLOAT,
  name  VARCHAR(50),
  PRIMARY KEY (id)
);
/

insert into Person (id, age, salary, name) values (1, 25, 400.25, 'John');
/
insert into Person (id, age, salary, name) values (2, 35, 600.25, 'Anne');
/
insert into Person (id, age, salary, name) values (3, 45, 600.25, 'Mary');
/
insert into Person (id, age, salary, name) values (10, 22, 100.25, 'Peter');
/
