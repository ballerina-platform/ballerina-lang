import ballerina/mysql;
import ballerina/io;
import ballerina/log;

function main(string... args) {

    endpoint mysql:Client testDBEP {
        host:"localhost",
        port:3306,
        name:"testdb",
        username:"root",
        password:"root",
        poolOptions:{maximumPoolSize:5}
    };

    //Create the tables required for the transaction.
    var ret = testDBEP->update("CREATE TABLE IF NOT EXISTS CUSTOMER (ID INT, NAME VARCHAR(30))");
    match ret {
        int retInt => log:printInfo("CUSTOMER table create status in DB:" + retInt);
        error err => {
            handleError("CUSTOMER table Creation failed", err, testDBEP);
            return;
        }
    }
    ret = testDBEP->update("CREATE TABLE IF NOT EXISTS SALARY (ID INT, MON_SALARY FLOAT)");
    match ret {
        int retInt => log:printInfo("SALARY table create status in DB:" + retInt);
        error err => {
            handleError("SALARY table Creation failed", err, testDBEP);
            return;
        }
    }
    //Here is the transaction block. Any transacted action within the transaction block may
    //return errors backend DB errors, connection pool errors, etc., You can decide whether
    //to abort or retry based on the error returned.
    //If you do not explicitly abort or retry, transaction will be
    //automatically retried  until the retry count is reached and aborted.
    //The retry count which is given with `retries` is the number of times the transaction
    //is retried before aborting it.
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
    testDBEP.stop();
}

function onCommitFunction(string transactionId) {
    io:println("Transaction: " + transactionId + " committed");
}

function onAbortFunction(string transactionId) {
    io:println("Transaction: " + transactionId + " aborted");
}

function handleError(string message, error e, mysql:Client testDB) {
    log:printError(message, err = e);
    testDB.stop();
}
