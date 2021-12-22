CREATE TABLE IF NOT EXISTS Customers(
  customerId INTEGER AUTO_INCREMENT,
  name  VARCHAR(300),
  creditLimit DOUBLE,
  country  VARCHAR(300),
  PRIMARY KEY (customerId)
);
/
CREATE ALIAS JAVAFUNC FOR "org.ballerinax.jdbc.actions.H2ActionsTest.javafunc";
/
INSERT INTO Customers(name, creditLimit, country) VALUES ('Oliver', 200000, 'UK');
/
INSERT INTO Customers(name, creditLimit, country) VALUES ('Barry', 200000, 'UK');
/
