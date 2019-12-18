import ballerina/io;
import ballerinax/java.jdbc;

// JDBC Client for H2 database.
jdbc:Client testDB = new ({
    url: "jdbc:h2:file:./local-transactions/testdb",
    username: "test",
    password: "test"
});

public function main() {
    // Create the tables that are required for the transaction.
    var ret = testDB->update("CREATE TABLE CUSTOMER (ID INTEGER, NAME " +
                              "VARCHAR(30))");
    handleUpdate(ret, "Create CUSTOMER table");

    // Populate table with data.
    ret = testDB->update("CREATE TABLE SALARY (ID INTEGER, MON_SALARY FLOAT)");
    handleUpdate(ret, "Create SALARY table");

    // This is a `transaction` block. If you do not explicitly `abort` or
    // `retry` a returned error, the transaction will be automatically
    // retried until the retry count is reached and aborted.
    // The retry count that is given via `retries` is the number of times the
    // transaction is retried before it being aborted and the default value is 3.
    transaction with retries = 4 {
        // Any transacted action within the `transaction` block may return
        // errors such as backend DB errors, connection pool errors etc.
        // The user can decide whether to `abort` or `retry` based on the
        // returned error.
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
        // Any action that needs to be performed after the transaction is
        // committed should be added here.
        io:println("Transaction committed");
    } aborted {
        // If the transaction is aborted, any action that needs to perform after
        // the abortion should be added here.
        io:println("Transaction aborted");
    }

    // Drop the tables.
    ret = testDB->update("DROP TABLE CUSTOMER");
    handleUpdate(ret, "Drop table CUSTOMER");

    ret = testDB->update("DROP TABLE SALARY");
    handleUpdate(ret, "Drop table SALARY");
}

// Function to handle the return value of the `update` remote function.
function handleUpdate(jdbc:UpdateResult|error returned, string message) {
    if (returned is jdbc:UpdateResult) {
        io:println(message + " status: ", returned.updatedRowCount);
    } else {
        io:println(message + " failed: ", <string>returned.detail()?.message);
    }
}
