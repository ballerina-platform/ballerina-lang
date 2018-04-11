import ballerina/sql;
import ballerina/io;

function main (string[] args) {

    endpoint sql:Client testDBEP {
        database:sql:DB_MYSQL,
        host:"localhost",
        port:3306,
        name:"testdb",
        username:"root",
        password:"root",
        options:{maximumPoolSize:5}
    };

    int updatedRows;
    //Create the tables required for the transaction.
    var updatedRowsResult = testDBEP -> update("CREATE TABLE IF NOT EXISTS CUSTOMER (ID INT,
        NAME VARCHAR(30))", null);
    match updatedRowsResult {
        int rows => {
            updatedRows = rows;
        }
        sql:SQLConnectorError err => {
            io:println("CUSTOMER table Creation failed:" + err.message);
            return;
        }
    }
    updatedRowsResult = testDBEP -> update("CREATE TABLE IF NOT EXISTS SALARY (ID INT,
        MON_SALARY FLOAT)", null);
    match updatedRowsResult {
        int rows => {
            updatedRows = rows;
        }
        sql:SQLConnectorError err => {
            io:println("SALARY table Creation failed:" + err.message);
            return;
        }
    }
    //Here is the transaction block. Since the update action may throw errors due to SQL errors, 
    //connection pool errors, etc., you can use a `try-catch` here to handle any exceptions. 
    // The `retry count` is the number of times the transaction is retried before aborting it. 
    // By default, a transaction is tried three times before aborting. Only integer literals 
    // or constants are allowed for `retry count`.
    boolean transactionSuccess = false;
    transaction with retries=4 {
        //This is the first action participant in the transaction.
        var result = testDBEP -> update("INSERT INTO CUSTOMER(ID,NAME) VALUES (1, 'Anne')", null);
        //This is the second action participant in the transaction.
        result = testDBEP -> update("INSERT INTO SALARY (ID, MON_SALARY) VALUES (1, 2500)", null);
        match result {
            int c => {
                io:println("Inserted count:" + c);
                // The transaction can be force aborted using the `abort` keyword at any time.
                if (c == 0) {
                    abort;
                }
            }
            sql:SQLConnectorError err => {
                fail;
            }
        }

        transactionSuccess = true;
        //The end curly bracket marks the end of the transaction and the transaction will
        //be committed or rolled back at this point.
    } onretry {
        //The failed block will be executed if the transaction fails due to an
        //exception or a throw statement. This block will execute each time
        //the transaction fails until it reaches the retry count.
        io:println("Transaction failed");
        transactionSuccess = false;
    }
    if (transactionSuccess) {
        io:println("Transaction committed");
    }
    //Close the connection pool.
    var closed = testDBEP -> close();
}
