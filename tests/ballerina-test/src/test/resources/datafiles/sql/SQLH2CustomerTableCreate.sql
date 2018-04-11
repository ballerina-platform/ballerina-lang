CREATE TABLE IF NOT EXISTS Customers(
  customerId INTEGER,
  name  VARCHAR(300),
  creditLimit DOUBLE,
  country  VARCHAR(300)
);
/
CREATE TABLE IF NOT EXISTS CustomersTrx(
  customerId INTEGER,
  name  VARCHAR(300),
  creditLimit DOUBLE,
  country  VARCHAR(300),
  PRIMARY KEY (customerId)
);
/
INSERT INTO CustomerTrx VALUES (30, 'Oliver', 200000, 'UK');
/
