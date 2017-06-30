# Transactions

Transaction processing is information processing that is divided into individual, indivisible operations called transactions. Each transaction must succeed or fail as a complete unit; it can never be only partially complete. 

A transaction is a series of data manipulation statements that must either fully complete or fully fail, leaving the system in a consistent state. It symbolizes a unit of work performed and treated in a coherent and reliable way independent of other transactions. There are two types of transactions; local and distributed.

## Local Transactions

Local transactions are transactions associated with a particular datasource (this means they are resource-specific). The most common example is a transaction associated with a JDBC connection.

## Distributed Transactions: (Global/XA Transactions)

A distributed transaction is a transaction that accesses and updates data on two or more networked resources, and therefore must be coordinated among those resources. This coordination is the function of the transaction manager. The transaction manager is responsible for making the final decision either to commit or rollback any distributed transaction and it uses the two phase commit protocol for that.

- First phase: The transaction manager polls all of the resource managers involved in the distributed transaction to see if each one is ready to commit. If a resource manager cannot commit, it responds negatively and rolls back its particular part of the transaction so that data is not altered.
- Second phase: The transaction manager determines if any of the resource managers have responded negatively, and, if so, rolls back the whole transaction. If there are no negative responses, the translation manager commits the whole transaction, and returns the results to the application.

JTA specifies standard Java interfaces between the transaction manager and the other components in a distributed transaction: the application, the application server, and the resource managers. [Atomikos](https://www.atomikos.com/) is used as open source JTA implementation.

A `transaction` is defined as follows:

```
transaction { 

   sql:ClientConnector.update(testdb1,...) 


   sql:ClientConnector.update(testdb2,...) 

	If ( ...) {
		abort;
	}


   ....
   //commit all local &	 XA transactions and close connections
} aborted { 
   //Logic which needs to execute on transaction failure
   ...

   ...
}

```
The `aborted` section is executed after the transaction is rolled back due to any of the following conditions:
- an exception occurred within the transaction block
- abort statement
- throw statement

### Nested Transactions

A `nested transaction` is defined as follows:

```
transaction { 

   ....

   ....

	transaction {

	   ....

	   ....

	} 
	aborted { 
	   ...

	   ...
	}
   ....
   
   ....


} 
aborted { 
   //Logic which needs to execute on transaction failure
   ...

   ...
}
```
Ballerina allows you to have transactions within a transaction.  All internal transactions are committed or aborted at the end of the outer most transaction block. If any of the transactions fail, the aborted block gets executed and all the transactions are rollbacked. The transaction is successful if all the nested transactions are successfully committed.

## Example

The following is an example of a local transaction where the transaction gets aborted if any of the `update` actions fail.

```
    map propertiesMap = {"jdbcUrl":"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
                         "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);
    sql:Parameter[] parameters = [];
    transaction {
    sql:ClientConnector.update(testDB, "Insert into Customers
                (firstName,lastName,registrationID,creditLimit,country) values ('James', 'Clerk', 200, 5000.75, 'USA')",
                               parameters);
    sql:ClientConnector.update(testDB, "Insert into Customers
                (firstName,lastName,registrationID,creditLimit,country) values ('James', 'Clerk', 200, 5000.75, 'USA')",
                               parameters);
    } aborted {
        system.println("The transaction is aborted");
    }
    sql:ClientConnector.close(testDB);

```

The following is an example of the `abort` statement in a transaction.

```
    map propertiesMap = {"jdbcUrl":"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
                         "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);
    sql:Parameter[] parameters = [];
    transaction {
        int insertCount = sql:ClientConnector.update(testDB, "Insert into Customers
                (firstName,lastName,registrationID,creditLimit,country) values ('James', 'Clerk', 220, 5000.75, 'USA')",
                                                     parameters);

        insertCount = sql:ClientConnector.update(testDB, "Insert into Customers
                (firstName,lastName,registrationID,creditLimit,country) values ('James', 'Clerk', 220, 5000.75, 'USA')",
                                             parameters);
        int i = 0;
        if (i == 0) {
            abort;//transaction can be aborted based on a condition
        }
    } aborted {
        system.println("The transaction is aborted");
    }
    sql:ClientConnector.close(testDB);
```
