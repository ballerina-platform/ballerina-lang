CREATE TABLE Customers(
  customerId INT,
  firstName  VARCHAR(300),
  lastName  VARCHAR(300),
  registrationId INT,
  creditLimit DECIMAL(10,2),
  country  VARCHAR(300),
  visaAccepted BOOLEAN,
  info BLOB(100),
  PRIMARY KEY (customerId)
);
/
INSERT INTO Customers (customerId, firstName, lastName, registrationID, creditLimit, country, visaAccepted, info)
  VALUES (1, 'Peter', 'Stuart', 1, 5000.75, 'USA', true, X'77736F322062616C6C6572696E6120626C6F6220746573742E' );
/
