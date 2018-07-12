import ballerina/jdbc;
import ballerina/io;

// Create an endpoint for H2 database. Change the DB details before running the sample.
endpoint jdbc:Client testDB {
    url: "jdbc:h2:file:./local-transactions/Testdb",
    username: "root",
    password: "root",
    poolOptions: { maximumPoolSize: 5 }
};

function main(string... args) {
    // Create the tables required for the transaction.
    var ret = testDB->update("CREATE TABLE CUSTOMER (ID INTEGER, NAME
                              VARCHAR(30))");
    handleUpdate(ret, "Create CUSTOMER table");

    ret = testDB->update("CREATE TABLE SALARY (ID INTEGER, MON_SALARY FLOAT)");
    handleUpdate(ret, "Create SALARY table");

    // Here is the transaction block. Any transacted action within the transaction block
    // may return errors like backend DB errors, connection pool errors, etc. User can
    // decide whether to abort or retry based on the error returned. If you do not
    // explicitly abort or retry on a returned error, transaction will be automatically
    // retried until the retry count is reached and aborted.
    // The retry count that is given with `retries` is the number of times the transaction
    // is retried before aborting it. By default, a transaction is tried three times before
    // aborting. Only integer literals or constants are allowed for `retry count`.
    // Two functions can be registered with `oncommit` and `onabort`. Those functions will be
    // executed at the end when the transaction is either aborted or committed.
    transaction with retries = 4, oncommit = onCommitFunction,
                                  onabort = onAbortFunction {
    // This is the first action participant in the transaction.
        var result = testDB->update("INSERT INTO CUSTOMER(ID,NAME)
                                     VALUES (1, 'Anne')");
        // This is the second action participant in the transaction.
        result = testDB->update("INSERT INTO SALARY (ID, MON_SALARY)
                                 VALUES (1, 2500)");
        match result {
            int c => {
                io:println("Inserted count: " + c);
                // If the transaction is forced to abort, it will roll back the transaction
                // and exit the transaction block without retrying.
                if (c == 0) {
                    abort;
                }
            }
            error err => {
                // If the transaction is forced to retry, it will roll back the transaction,
                // go to the `onretry` block, and retry from the beginning until the defined
                // retry count is reached.
                retry;
            }
        }
    // The end curly bracket marks the end of the transaction, and the transaction will
    // be committed or rolled back at this point.
    } onretry {
        // The `onretry` block will be executed whenever the transaction is retried until it
        // reaches the retry count. A transaction could be retried if it fails due to an
        // exception or throw statement, or from an explicit retry statement.
        io:println("Retrying transaction");
    }

    //Drop the tables.
    ret = testDB->update("DROP TABLE CUSTOMER");
    handleUpdate(ret, "Drop table CUSTOMER");

    ret = testDB->update("DROP TABLE SALARY");
    handleUpdate(ret, "Drop table SALARY");

    // Close the connection pool.
    testDB.stop();
}

// This is the function used as the commit handler of the transaction block. Any action
// that needs to perform once the transaction is committed should go here.
function onCommitFunction(string transactionId) {
    io:println("Transaction: " + transactionId + " committed");
}

// This is the function used as the abort handler of the transaction block. Any action
// that needs to perform if the transaction is aborted should go here.
function onAbortFunction(string transactionId) {
    io:println("Transaction: " + transactionId + " aborted");
}

// Function to handle return of the update operation.
function handleUpdate(int|error returned, string message) {
    match returned {
        int retInt => io:println(message + " status: " + retInt);
        error err => io:println(message + " failed: " + err.message);
    }
}
