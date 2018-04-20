import ballerina/mysql;
import ballerina/io;

function main(string... args) {

    endpoint mysql:Client testDBEP {
        host:"localhost",
        port:3306,
        name:"testdb",
        username:"root",
        password:"root",
        poolOptions:{maximumPoolSize:5}
    };

    int updatedRows;
    //Create the tables required for the transaction.
    var updatedRowsResult = testDBEP->update("CREATE TABLE IF NOT EXISTS CUSTOMER (ID INT, NAME VARCHAR(30))");
    match updatedRowsResult {
        int rows => {
            updatedRows = rows;
        }
        error err => {
            io:println("CUSTOMER table Creation failed:" + err.message);
            return;
        }
    }
    updatedRowsResult = testDBEP->update("CREATE TABLE IF NOT EXISTS SALARY (ID INT, MON_SALARY FLOAT)");
    match updatedRowsResult {
        int rows => {
            updatedRows = rows;
        }
        error err => {
            io:println("SALARY table Creation failed:" + err.message);
            return;
        }
    }
    //Here is the transaction block. Since the update action may throw errors due to SQL errors,
    //connection pool errors, etc., you can use a `try-catch` here to handle any exceptions.
    //The `retry count` is the number of times the transaction is retried before aborting it.
    //By default, a transaction is tried three times before aborting. Only integer literals
    //or constants are allowed for `retry count`.
    transaction with retries = 4, oncommit = onCommitFunction, onabort = onAbortFunction {
    //This is the first action participant in the transaction.
        var result = testDBEP->update("INSERT INTO CUSTOMER(ID,NAME) VALUES (1, 'Anne')");
        //This is the second action participant in the transaction.
        result = testDBEP->update("INSERT INTO SALARY (ID, MON_SALARY) VALUES (1, 2500)");
        match result {
            int c => {
                io:println("Inserted count: " + c);
                // The transaction can be force aborted using the `abort` keyword at any time.
                if (c == 0) {
                    abort;
                }
            }
            error err => {
                // The transaction can be force retried using `retry` keyword at any time.
                retry;
            }
        }
    //The end curly bracket marks the end of the transaction and the transaction will
    //be committed or rolled back at this point.
    } onretry {
    //The onretry block will be executed whenever the transaction is retried until it reaches the retry count.
    //Transaction could be re-tried if it fails due to an exception or a throw statement, or an explicit retry
    //statement.
        io:println("Retrying transaction");
    }
    //Close the connection pool.
    var closed = testDBEP->close();
}

function onCommitFunction(string transactionId) {
    io:println("Transaction: " + transactionId + " committed");
}

function onAbortFunction(string transactionId) {
    io:println("Transaction: " + transactionId + " aborted");
}
