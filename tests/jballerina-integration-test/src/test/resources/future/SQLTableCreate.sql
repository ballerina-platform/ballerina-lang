CREATE TABLE IF NOT EXISTS FactoryEmployee(
  empId INTEGER NOT NULL IDENTITY,
  firstName  VARCHAR(300),
  lastName  VARCHAR(300),
  registrationID INTEGER,
  creditLimit DOUBLE,
  country  VARCHAR(300),
  PRIMARY KEY (customerId)
);
