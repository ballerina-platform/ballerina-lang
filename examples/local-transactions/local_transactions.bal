import ballerina/io;
import ballerinax/java.jdbc;

// Creates an endpoint for the H2 database. Changes the DB details before running the example.
jdbc:Client testDB = new ({
    url: "jdbc:h2:file:./local-transactions/testdb",
    username: "test",
    password: "test",
    poolOptions: { maximumPoolSize: 5 }
});

public function main() {
    // Creates the tables that are required for the transaction.
    var ret = testDB->update("CREATE TABLE CUSTOMER (ID INTEGER, NAME " +
                              "VARCHAR(30))");
    handleUpdate(ret, "Create CUSTOMER table");

    ret = testDB->update("CREATE TABLE SALARY (ID INTEGER, MON_SALARY FLOAT)");
    handleUpdate(ret, "Create SALARY table");

    // The below is a `transaction` block. Any transacted action within the `transaction` block
    // may return errors such as backend DB errors, connection pool errors etc. The user can
    // decide whether to `abort` or `retry` based on the returned error. If you do not
    // explicitly `abort` or `retry` a returned error, the transaction will be automatically
    // retried until the retry count is reached and aborted.
    // The retry count that is given via `retries` is the number of times the transaction
    // is retried before it being aborted. By default, a transaction is tried three times before
    // aborting it. Only integer literals or constants are allowed as the `retry count`.
    transaction with retries = 4 {
        // This is the first remote function participant of the transaction.
        ret = testDB->update("INSERT INTO CUSTOMER(ID,NAME) " +
                                     "VALUES (1, 'Anne')");
        // This is the second remote function participant of the transaction.
        ret = testDB->update("INSERT INTO SALARY (ID, MON_SALARY) " +
                                 "VALUES (1, 2500)");
        if (ret is jdbc:UpdateResult) {
            io:println("Inserted count: ", ret.updatedRowCount);
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
    // The end curly bracket marks the end of the transaction and the transaction will
    // be committed or rolled back at this point.
    } onretry {
        // The `onretry` block will be executed whenever the transaction is retried until it
        // reaches the retry count. A transaction could be retried if it fails due to an
        // exception or throw statement or from an explicit retry statement.
        io:println("Retrying transaction");
    } committed {
        // Any action that needs to be performed once the transaction is committed should be added as shown below.
        io:println("Transaction committed");
    } aborted {
        // Any action that needs to perform if the transaction is aborted should be added as shown below.
        io:println("Transaction aborted");
    }

    //Drops the tables.
    ret = testDB->update("DROP TABLE CUSTOMER");
    handleUpdate(ret, "Drop table CUSTOMER");

    ret = testDB->update("DROP TABLE SALARY");
    handleUpdate(ret, "Drop table SALARY");

    // Closes the connection pool.
    var stopRet = testDB.stop();
    if (stopRet is error) {
        io:println(stopRet.detail()["message"]);
    }
}

// This function handles the return of the update operation.
function handleUpdate(jdbc:UpdateResult|error returned, string message) {
    if (returned is jdbc:UpdateResult) {
        io:println(message + " status: ", returned.updatedRowCount);
    } else {
        io:println(message + " failed: ", returned.reason());
    }
}
