import ballerina/io;
import ballerina/jdbc;
import ballerina/sql;

// Create an endpoint for the first database named testdb1. Since this endpoint
// participates in a distributed transaction, the `isXA` property should be true.
endpoint jdbc:Client testDB1 {
    url: "jdbc:h2:file:./xa-transactions/Testdb1",
    username: "root",
    password: "root",
    poolOptions: { maximumPoolSize: 5, isXA: true }
};

// Create an endpoint for the second database named testdb2. Since this endpoint
// participates in a distributed transaction, the `isXA` property should be true.
endpoint jdbc:Client testDB2 {
    url: "jdbc:h2:file:./xa-transactions/Testdb2",
    username: "root",
    password: "root",
    poolOptions: { maximumPoolSize: 5, isXA: true }
};

function main(string... args) {

    // Create the table named CUSTOMER in the first database.
    var ret = testDB1->update("CREATE TABLE CUSTOMER (ID INTEGER
                    AUTO_INCREMENT, NAME VARCHAR(30))");
    handleUpdate(ret, "Create CUSTOMER table");
    // Create the table named SALARY in the second database.
    ret = testDB2->update("CREATE TABLE SALARY (ID INT, VALUE FLOAT)");
    handleUpdate(ret, "Create SALARY table");

    // Begins the transaction.
    transaction with oncommit = onCommitFunction,
    onabort = onAbortFunction {
    // This is the first action to participate in the transaction. It inserts customer
    // name to the first DB and gets the generated key.
        var retWithKey = testDB1->updateWithGeneratedKeys("INSERT INTO
                                CUSTOMER(NAME) VALUES ('Anne')", ());
        string generatedKey;
        match retWithKey {
            (int, string[]) y => {
                var (count, ids) = y;
                generatedKey = ids[0];
                io:println("Inserted row count: " + count);
                io:println("Generated key: " + generatedKey);
            }
            error err => io:println("Insert to student table failed: "
                    + err.message);
        }

        //Converte the returned key into integer.
        ret = <int>generatedKey;
        int key = -1;
        match ret {
            int retInt => key = retInt;
            error err => io:println("Converting key to string failed: "
                    + err.message);
        }
        io:println("Generated key for the inserted row: " + key);
        // This is the second action to participate in the transaction. It inserts the
        // salary info to the second DB along with the key generated in the first DB.
        ret = testDB2->update("INSERT INTO SALARY (ID, VALUE)
                               VALUES (?, ?)", key, 2500);
        handleUpdate(ret, "Insert to SALARY table");
    } onretry {
        io:println("Retrying transaction");
    }

    // Drop the tables created for this sample.
    ret = testDB1->update("DROP TABLE CUSTOMER");
    handleUpdate(ret, "Drop Table CUSTOMER");

    ret = testDB2->update("DROP TABLE SALARY");
    handleUpdate(ret, "Drop Table SALARY");

    // Close the connection pool.
    testDB1.stop();
    testDB2.stop();
}

function onCommitFunction(string transactionId) {
    io:println("Transaction: " + transactionId + " committed");
}

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
