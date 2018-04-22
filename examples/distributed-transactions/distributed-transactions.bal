import ballerina/mysql;
import ballerina/sql;
import ballerina/io;

function main(string... args) {
    //Create an endpoint for the first database named testdb1. Since this endpoint is
    //participated in a distributed transaction, the isXA property should be true.
    endpoint mysql:Client testDBEP1 {
        host:"localhost",
        port:3306,
        name:"testdb1",
        username:"root",
        password:"root",
        poolOptions:{maximumPoolSize:5}
    };

    //Create an endpoint for the second database named testdb2. Since this endpoint is
    //participated in a distributed transaction, the isXA property of the
    //sql:ClientConnector should be true.
    endpoint mysql:Client testDBEP2 {
        host:"localhost",
        port:3306,
        name:"testdb2",
        username:"root",
        password:"root",
        poolOptions:{maximumPoolSize:5}
    };


    //Create the table named CUSTOMER in the first database.
    var ret = testDBEP1->update("CREATE TABLE CUSTOMER (ID INT AUTO_INCREMENT PRIMARY KEY,
                                    NAME VARCHAR(30))");
    match ret {
        int retInt => io:println("CUSTOMER table create status in first DB:" + retInt);
        error err => {
            io:println("CUSTOMER table Creation failed:" + err.message);
            return;
        }
    }
    //Create the table named SALARY in the second database.
    ret = testDBEP2->update("CREATE TABLE SALARY (ID INT, VALUE FLOAT)");
    match ret {
        int retInt => io:println("SALARY table create status in second DB:" + retInt);
        error err => {
            io:println("SALARY table Creation failed:" + err.message);
            return;
        }
    }

    //Begins the transaction.
    transaction with oncommit = onCommitFunction, onabort = onAbortFunction {
    //This is the first action participate in the transaction which insert customer
    //name to the first DB and get the generated key.
        int insertCount;
        string[] generatedID;
        var out = testDBEP1->updateWithGeneratedKeys("INSERT INTO
                                     CUSTOMER(NAME) VALUES ('Anne')", ());
        match out {
            (int, string[]) output => (insertCount, generatedID) = output;
            error err => throw err.cause but { () => err };
        }
        var returnedKey = check <int>generatedID[0];
        io:println("Inserted count to CUSTOMER table: " + insertCount);
        io:println("Generated key for the inserted row: " + returnedKey);
        //This is the second action participate in the transaction which insert the
        //salary info to the second DB along with the key generated in the first DB.
        sql:Parameter para1 = (sql:TYPE_INTEGER, returnedKey);
        ret = testDBEP2->update("INSERT INTO SALARY (ID, VALUE) VALUES (?, 2500)", para1);
        match ret {
            int retInt => io:println("Inserted count to SALARY table: " + retInt);
            error err => retry;
        }

    } onretry {
        io:println("Retrying transaction");
    }

    //Drop the tables created for this sample.
    ret = testDBEP1->update("DROP TABLE CUSTOMER");
    match ret {
        int retInt => io:println("CUSTOMER table drop status: " + retInt);
        error err => throw err.cause but { () => err };
    }
    ret = testDBEP2->update("DROP TABLE SALARY");
    match ret {
        int retInt => io:println("SALARY table drop status: " + retInt);
        error err => throw err.cause but { () => err };
    }

    //Close the connection pool.
    testDBEP1.stop();
    testDBEP2.stop();
}

function onCommitFunction(string transactionId) {
    io:println("Transaction: " + transactionId + " committed");
}

function onAbortFunction(string transactionId) {
    io:println("Transaction: " + transactionId + " aborted");
}
