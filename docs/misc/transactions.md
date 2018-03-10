# Transactions

Transaction processing is information processing that is divided into individual, indivisible operations called transactions. Each transaction must succeed or fail as a complete unit; it can never be only partially complete. 

A transaction is a series of data manipulation statements that must either fully complete or fully fail, leaving the system in a consistent state. It symbolizes a unit of work performed and treated in a coherent and reliable way independent of other transactions. There are two types of transactions; local and distributed.

## Local Transactions

Local transactions are transactions associated with a particular datasource (this means they are resource-specific). The most common example is a transaction associated with a JDBC connection.

## Distributed Transactions: (Global/XA Transactions)

A distributed transaction is a transaction that accesses and updates data on two or more networked resources, and therefore must be coordinated among those resources. This coordination is the function of the transaction manager. The transaction manager is responsible for making the final decision either to commit or rollback any distributed transaction and it uses the two phase commit protocol for that.

- First phase: The transaction manager polls all of the resource managers involved in the distributed transaction to see if each one is ready to commit. If a resource manager cannot commit, it responds negatively and rolls back its particular part of the transaction so that data is not altered.
- Second phase: The transaction manager determines if any of the resource managers have responded negatively, and, if so, rolls back the whole transaction. If there are no negative responses, the translation manager commits the whole transaction, and returns the results to the application.

JTA specifies standard Java interfaces between the transaction manager and the other components in a distributed transaction: the application, the application server, and the resource managers. 

A `transaction` is defined as follows:

```ballerina
transaction { 

   testdb1.update(...) 


   testdb2.update(...) 

	If ( ...) {
		abort;
	}


   ....
   //commit all local &	 XA transactions and close connections
} failed { 
   //Logic which needs to execute on transaction failure
   ...

   ...
}

```

### Nested Transactions

A `nested transaction` is defined as follows:

```ballerina
transaction { 

   ....

   ....

	transaction {

	   ....

	   ....

	} failed { 
	   ...

	   ...
	}
   ....
   
   ....


} 

```
Ballerina allows you to have transactions within a transaction.  All internal transactions are committed or aborted at the end of the outer most transaction block.

## Example

The following is an example of a local transaction where the transaction gets aborted if any of the `update` actions fail.

```ballerina
    import ballerina.data.sql;
    
    function main (string[] args) {
        endpoint<sql:ClientConnector> testDB {
          create sql:ClientConnector(
            sql:DB.MYSQL, "localhost", 3306, "testdb", "root", "root", {maximumPoolSize:5});
        }
        boolean transactionSuccess = false;
        transaction with retries(4){
            int c = testDB.update("INSERT INTO CUSTOMER(ID,NAME) VALUES (1, 'Anne')", null);
            c = testDB.update("INSERT INTO SALARY (ID, MON_SALARY) VALUES (1, 2500)", null);
            println("Inserted count:" + c);
            if (c == 0) {
                abort;
            }
            transactionSuccess = true;
        } failed {
            transactionSuccess = false;
        }
        if (transactionSuccess) {
            println("Transaction committed");
        }
        testDB.close();
    }


```