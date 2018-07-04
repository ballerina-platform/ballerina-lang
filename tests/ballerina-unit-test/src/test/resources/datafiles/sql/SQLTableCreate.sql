CREATE TABLE IF NOT EXISTS Customers(
  customerId INTEGER NOT NULL IDENTITY,
  firstName  VARCHAR(300),
  lastName  VARCHAR(300),
  registrationID INTEGER,
  creditLimit DOUBLE,
  country  VARCHAR(300),
  PRIMARY KEY (customerId)
);
/
insert into Customers (firstName,lastName,registrationID,creditLimit,country)
  values ('Peter', 'Stuart', 1, 5000.75, 'USA');
/
CREATE TABLE IF NOT EXISTS CustomersTrx(
  customerId INTEGER NOT NULL IDENTITY,
  firstName  VARCHAR(300),
  lastName  VARCHAR(300),
  registrationID INTEGER,
  creditLimit DOUBLE,
  country  VARCHAR(300),
  PRIMARY KEY (customerId)
);
/
CREATE TABLE IF NOT EXISTS CustomersTrx2(
  customerId INTEGER NOT NULL,
  firstName  VARCHAR(300),
  lastName  VARCHAR(300),
  registrationID INTEGER,
  creditLimit DOUBLE,
  country  VARCHAR(300),
  PRIMARY KEY (customerId)
);
/
insert into CustomersTrx (firstName,lastName,registrationID,creditLimit,country)
  values ('Peter', 'Stuart', 1, 5000.75, 'USA');
/
insert into CustomersTrx2 (customerId,firstName,lastName,registrationID,creditLimit,country)
  values (1, 'Peter', 'Stuart', 1, 5000.75, 'USA');
/
