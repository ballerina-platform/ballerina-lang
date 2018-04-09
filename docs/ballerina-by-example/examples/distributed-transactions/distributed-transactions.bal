import ballerina/sql;
import ballerina/io;

function main (string[] args) {
    //Create an endpoint for the first database named testdb1. Since this endpoint is
    //participated in a distributed transaction, the isXA property should be true.
    endpoint sql:Client testDBEP1 {
        database: sql:DB_MYSQL,
        host: "localhost",
        port: 3306,
        name: "testdb1",
        username: "root",
        password: "root",
        options: {maximumPoolSize:5}
    };

    //Create an endpoint for the second database named testdb2. Since this endpoint is
    //participated in a distributed transaction, the isXA property of the
    //sql:ClientConnector should be true.
    endpoint sql:Client testDBEP2 {
        database: sql:DB_MYSQL,
        host: "localhost",
        port: 3306,
        name: "testdb2",
        username: "root",
        password: "root",
        options: {maximumPoolSize:5}
    };

    //Create the table named CUSTOMER in the first database.
    var ret = testDBEP1 -> update("CREATE TABLE CUSTOMER (ID INT AUTO_INCREMENT PRIMARY KEY,
                                    NAME VARCHAR(30))", null);
    match ret {
        int retInt =>  io:println("CUSTOMER table create status in first DB:" + retInt);
        sql:SQLConnectorError err => {
            io:println("CUSTOMER table Creation failed:" + err.message);
            return;
        }
    }
    //Create the table named SALARY in the second database.
    ret = testDBEP2 -> update("CREATE TABLE SALARY (ID INT, VALUE FLOAT)", null);
    match ret {
        int retInt =>  {
            io:println("SALARY table create status in second DB:" + retInt);
        }
        sql:SQLConnectorError err => {
            io:println("SALARY table Creation failed:" + err.message);
            return;
        }
    }

    boolean transactionSuccess = false;
    //Begins the transaction.
    transaction {
        //This is the first action participate in the transaction which insert customer
        //name to the first DB and get the generated key.
        int insertCount; string[] generatedID;
        var out = testDBEP1 -> updateWithGeneratedKeys("INSERT INTO
                                     CUSTOMER(NAME) VALUES ('Anne')", null, null);
        match out {
            (int, string[]) output =>
                (insertCount, generatedID) = output;
            sql:SQLConnectorError err => {
                throw err.cause[0];
            }
        }
        var returnedKey = check <int>generatedID[0];
        io:println("Inserted count to CUSTOMER table:" + insertCount);
        io:println("Generated key for the inserted row:" + returnedKey);
        //This is the second action participate in the transaction which insert the
        //salary info to the second DB along with the key generated in the first DB.
        sql:Parameter para1 = {sqlType:sql:TYPE_INTEGER, value:returnedKey};
        sql:Parameter[] params = [para1];
        ret = testDBEP2 -> update("INSERT INTO SALARY (ID, VALUE) VALUES (?, 2500)", params);
        match ret {
            int retInt =>  {
                io:println("Inserted count to SALARY table:" + retInt);
            }
            sql:SQLConnectorError err => {
                fail;
            }
        }

        transactionSuccess = true;
    } onretry {
        io:println("Transaction failed");
        transactionSuccess = false;
    }
    if (transactionSuccess) {
        io:println("Transaction committed");
    }
    //Drop the tables created for this sample.
    ret = testDBEP1 -> update("DROP TABLE CUSTOMER", null);
    match ret {
        int retInt =>  {
            io:println("CUSTOMER table drop status:" + retInt);
        }
        sql:SQLConnectorError err => {
            throw err.cause[0];
        }
    }
    ret = testDBEP2 -> update("DROP TABLE SALARY", null);
    match ret {
        int retInt =>  {
            io:println("SALARY table drop status:" + retInt);
        }
        sql:SQLConnectorError err => {
            throw err.cause[0];
        }
    }

    //Close the connection pool.
    var closedCon1 = testDBEP1 -> close();
    var closedCon2 = testDBEP2 -> close();
}
