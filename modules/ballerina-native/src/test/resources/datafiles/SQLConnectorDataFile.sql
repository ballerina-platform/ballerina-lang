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
CREATE PROCEDURE InsertPersonData(IN p_RegID INTEGER, IN p_PersonName VARCHAR(50))
  MODIFIES SQL DATA
  BEGIN ATOMIC
  INSERT INTO Customers(registrationID, firstName,lastName, creditLimit,country)
    VALUES (p_RegID, p_PersonName, p_PersonName, 25000, 'UK');
  END
/