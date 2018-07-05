CREATE TABLE IF NOT EXISTS Customers(
  customerId INTEGER NOT NULL IDENTITY,
  name  VARCHAR(300),
  creditLimit DOUBLE,
  country  VARCHAR(300),
  PRIMARY KEY (customerId)
);
/
CREATE ALIAS JAVAFUNC FOR "org.ballerinalang.test.connectors.h2.H2ClientActionsTest.javafunc";
/
INSERT INTO Customers VALUES (1, 'Oliver', 200000, 'UK');
/
INSERT INTO Customers VALUES (2, 'Barry', 200000, 'UK');
/
