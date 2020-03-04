CREATE TABLE NumericTypes (
   id INT IDENTITY,
   int_type INT NOT NULL,
   bigint_type BIGINT NOT NULL,
   smallint_type SMALLINT NOT NULL ,
   tinyint_type TINYINT NOT NULL ,
   bit_type BIT NOT NULL ,
   decimal_type DECIMAL(10,3) NOT NULL ,
   numeric_type NUMERIC(10,3) NOT NULL ,
   float_type FLOAT NOT NULL ,
   real_type REAL NOT NULL ,
   PRIMARY KEY (id)
);
/
INSERT INTO NumericTypes (id, int_type, bigint_type, smallint_type, tinyint_type, bit_type, decimal_type, numeric_type,
    float_type, real_type) VALUES (1, 2147483647, 9223372036854774807, 32767, 127, 1, 1234.567, 1234.567, 1234.567,
    1234.567);
/

CREATE TABLE NumericNullTypes (
   id INT IDENTITY,
   int_type INT,
   bigint_type BIGINT,
   smallint_type SMALLINT,
   tinyint_type TINYINT,
   bit_type BIT,
   decimal_type DECIMAL(10,3),
   numeric_type NUMERIC(10,3),
   float_type FLOAT,
   real_type REAL,
   PRIMARY KEY (id)
);
/
INSERT INTO NumericNullTypes (id, int_type, bigint_type, smallint_type, tinyint_type, bit_type, decimal_type, numeric_type,
    float_type, real_type) VALUES (1, 2147483647, 9223372036854774807, 32767, 127, 1, 1234.567, 1234.567, 1234.567,
    1234.567);
/

INSERT INTO NumericNullTypes (id, int_type, bigint_type, smallint_type, tinyint_type, bit_type, decimal_type, numeric_type,
    float_type, real_type) VALUES (2, null , null , null , null , null , null , null , null ,
    null );
/

-- CREATE TABLE Customers(
--   customerId INT,
--   firstName  VARCHAR(300),
--   lastName  VARCHAR(300),
--   registrationId INT,
--   creditLimit DECIMAL(10,2),
--   country  VARCHAR(300),
--   visaAccepted BOOLEAN,
--   info BLOB(100),
--   PRIMARY KEY (customerId)
-- );
-- /
-- INSERT INTO Customers (customerId, firstName, lastName, registrationID, creditLimit, country, visaAccepted, info)
--   VALUES (1, 'Peter', 'Stuart', 1, 5000.75, 'USA', true, X'77736F322062616C6C6572696E6120626C6F6220746573742E' );
-- /