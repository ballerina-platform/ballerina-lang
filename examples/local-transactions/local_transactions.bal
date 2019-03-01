import ballerina/io;
import ballerina/h2;
import ballerina/sql;

// Create an endpoint for H2 database. Change the DB details before running the sample.
h2:Client testDB = new ({
    path: "./local-transactions/",
    name: "Testdb",
    username: "test",
    password: "test",
    poolOptions: { maximumPoolSize: 5 }
});

public function main() {
    // Create the tables required for the transaction.
    var ret = testDB->update("CREATE TABLE CUSTOMER (ID INTEGER, NAME
                              VARCHAR(30))");
    handleUpdate(ret, "Create CUSTOMER table");

    ret = testDB->update("CREATE TABLE SALARY (ID INTEGER, MON_SALARY FLOAT)");
    handleUpdate(ret, "Create SALARY table");

    // Here is the `transaction` block. Any transacted action within the `transaction` block
    // may return errors like backend DB errors, connection pool errors, etc. User can
    // decide whether to `abort` or `retry` based on the error returned. If you do not
    // explicitly `abort` or `retry` on a returned error, transaction will be automatically
    // retried until the retry count is reached and aborted.
    // The retry count that is given with `retries` is the number of times the transaction
    // is retried before aborting it. By default, a transaction is tried three times before
    // aborting. Only integer literals or constants are allowed for `retry count`.
    transaction with retries = 4 {
        // This is the first remote function participant in the transaction.
        ret = testDB->update("INSERT INTO CUSTOMER(ID,NAME)
                                     VALUES (1, 'Anne')");
        // This is the second remote function participant in the transaction.
        ret = testDB->update("INSERT INTO SALARY (ID, MON_SALARY)
                                 VALUES (1, 2500)");
        if (ret is sql:UpdateResult) {
            io:println("Inserted count: " + ret.updatedRowCount);
            // If the transaction is forced to abort, it will roll back the transaction
            // and exit the transaction block without retrying.
            if (ret.updatedRowCount == 0) {
                abort;
            }
        } else {
            // If the transaction is forced to retry, it will roll back the transaction,
            // go to the `onretry` block, and retry from the beginning until the defined
            // retry count is reached.
            retry;
        }
    // The end curly bracket marks the end of the transaction, and the transaction will
    // be committed or rolled back at this point.
    } onretry {
        // The `onretry` block will be executed whenever the transaction is retried until it
        // reaches the retry count. A transaction could be retried if it fails due to an
        // exception or throw statement, or from an explicit retry statement.
        io:println("Retrying transaction");
    } committed {
        // Any action that needs to perform once the transaction is committed should go here.
        io:println("Transaction committed");
    } aborted {
        // Any action that needs to perform if the transaction is aborted should go here.
        io:println("Transaction aborted");
    }

    //Drop the tables.
    ret = testDB->update("DROP TABLE CUSTOMER");
    handleUpdate(ret, "Drop table CUSTOMER");

    ret = testDB->update("DROP TABLE SALARY");
    handleUpdate(ret, "Drop table SALARY");

    // Close the connection pool.
    var stopRet = testDB.stop();
    if (stopRet is error) {
        io:println(stopRet.detail().message);
    }
}

// Function to handle return of the update operation.
function handleUpdate(sql:UpdateResult|error returned, string message) {
    if (returned is sql:UpdateResult) {
        io:println(message + " status: " + returned.updatedRowCount);
    } else {
        io:println(message + " failed: " + returned.reason());
    }
}
