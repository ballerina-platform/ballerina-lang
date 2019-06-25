CREATE TABLE IF NOT EXISTS Customers(
  customerId INTEGER NOT NULL IDENTITY,
  firstName  VARCHAR(300),
  lastName  VARCHAR(300),
  registrationID VARCHAR(300),
  creditLimit DOUBLE,
  country  VARCHAR(300),
  PRIMARY KEY (customerId)
);
/
insert into Customers (firstName,lastName,registrationID,creditLimit,country)
  values ('Peter', 'Stuart', '1', 5000.75, 'USA');
/
insert into Customers (firstName,lastName,registrationID,creditLimit,country)
  values ('John', 'Watson', '2', 2348.93, 'UK');
/
