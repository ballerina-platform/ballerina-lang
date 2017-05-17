# Local transactions

Local transactions are transactions associated with a particular data source (means they are resource-specific). The most common example would be a transaction associated with a JDBC connection.

# Distributed transaction: (Global/XA transaction)

A distributed transaction is a transaction that accesses and updates data on two or more networked resources, and therefore must be coordinated among those resources. This coordination is the function of the transaction manager.

- First phase: The transaction manager polls all of the resource managers involved in the distributed transaction to see if each one is ready to commit. If a resource manager cannot commit, it responds negatively and rolls back its particular part of the transaction so that data is not altered.
- Second phase: The transaction manager determines if any of the resource managers have responded negatively, and, if so, rolls back the whole transaction. If there are no negative responses, the translation manager commits the whole transaction, and returns the results to the application.

JTA specifies standard Java interfaces between the transaction manager and the other components in a distributed transaction: the application, the application server, and the resource managers. Atomikos is used as open source JTA implementation.

A `transaction` is defined as follows:

```
transaction { //Initialize BallerinaTransactionManager for the Context

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
A `transaction` can be aborted if an `If` condition is met and the `abort;` command is executed. It can also be aborted with specific logic using the `aborted` method.

A `nested transaction` is defined as follows:

```
transaction { //Initialize BallerinaTransactionManager for the Context - transactionLevel = 1

   ....

   ....

transaction { transactionLevel = 2

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
onRollback { 
   //Logic which needs to execute on transaction failure
   ...

   ...
}
```
Rollback happens after the `onRollback` block executes.

## Example

The following is an example of a local transaction.

```
function testLocalTransacton () (int, int) {
    map propertiesMap = {"jdbcUrl":"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
                         "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);
    int returnVal = 0;
    sql:Parameter[] parameters = [];
    transaction {
    sql:ClientConnector.update(testDB, "Insert into Customers
                (firstName,lastName,registrationID,creditLimit,country) values ('James', 'Clerk', 200, 5000.75, 'USA')",
                               parameters);
    sql:ClientConnector.update(testDB, "Insert into Customers
                (firstName,lastName,registrationID,creditLimit,country) values ('James', 'Clerk', 200, 5000.75, 'USA')",
                               parameters);
    } aborted {
        returnVal = - 1;
    }
    //check whether update action is performed
    int count;
    datatable dt = sql:ClientConnector.select(testDB, "Select COUNT(*) from Customers where registrationID = 200",
    parameters);
    while (datatables:next(dt)) {
        count = datatables:getInt(dt, 1);
    }
    datatables:close(dt);
    sql:ClientConnector.close(testDB);
    return returnVal, count;
}
```
The following is an example of the rollback function in a transaction.

```
function testTransactonRollback () (int, int) {
    map propertiesMap = {"jdbcUrl":"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
                         "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);
    int returnVal = 0;
    sql:Parameter[] parameters = [];
    transaction {
        sql:ClientConnector.update(testDB, "Insert into Customers
                (firstName,lastName,registrationID,creditLimit,country) values ('James', 'Clerk', 210, 5000.75, 'USA')",
                                   parameters);
        sql:ClientConnector.update(testDB, "Insert into Customers2
                (firstName,lastName,registrationID,creditLimit,country) values ('James', 'Clerk', 210, 5000.75, 'USA')",
                                   parameters);
    } aborted {
        returnVal = - 1;
    }
    //check whether update action is performed
    int count;
    datatable dt = sql:ClientConnector.select(testDB, "Select COUNT(*) from Customers where registrationID = 210",
        parameters);
    while (datatables:next(dt)) {
        count = datatables:getInt(dt, 1);
    }
    datatables:close(dt);
    sql:ClientConnector.close(testDB);
    return returnVal, count;
}
```

The following is an example of the abort function in a transaction.

```
function testTransactonAbort () (int, int) {
    map propertiesMap = {"jdbcUrl":"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
                         "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);
    int returnVal = 0;
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
            abort;
        }
    } aborted {
        returnVal = - 1;
    }
    //check whether update action is performed
    int count;
    datatable dt = sql:ClientConnector.select(testDB, "Select COUNT(*) from Customers where registrationID = 220",
        parameters);
    while (datatables:next(dt)) {
        count = datatables:getInt(dt, 1);
    }
    datatables:close(dt);
    sql:ClientConnector.close(testDB);
    return returnVal, count;
}
```
