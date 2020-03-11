CREATE TABLE IF NOT EXISTS Customers(
  customerId INTEGER NOT NULL AUTO_INCREMENT,
  firstName  VARCHAR(300),
  lastName  VARCHAR(300),
  registrationID INTEGER,
  creditLimit DOUBLE,
  country  VARCHAR(300),
  PRIMARY KEY (customerId)
);

INSERT INTO Customers (firstName,lastName,registrationID,creditLimit,country)
  VALUES ('Peter', 'Stuart', 1, 5000.75, 'USA');

INSERT INTO Customers (firstName,lastName,registrationID,creditLimit,country)
  VALUES ('Dan', 'Brown', 2, 10000, 'UK');
