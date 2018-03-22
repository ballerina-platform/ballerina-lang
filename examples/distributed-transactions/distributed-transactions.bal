import ballerina/data.sql;
import ballerina/io;

function main (string[] args) {
    //Create an endpoint for the first database named testdb1. Since this endpoint is
    //participated in a distributed transaction, the isXA property should be true.
    endpoint sql:Client testDBEP1 {
        database: sql:DB.MYSQL,
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
        database: sql:DB.MYSQL,
        host: "localhost",
        port: 3306,
        name: "testdb2",
        username: "root",
        password: "root",
        options: {maximumPoolSize:5}
    };

    var testDB1 = testDBEP1.getConnector();
    var testDB2 = testDBEP2.getConnector();

    //Create the table named CUSTOMER in the first database.
    int ret = testDB1 -> update("CREATE TABLE CUSTOMER (ID INT AUTO_INCREMENT PRIMARY KEY,
                                    NAME VARCHAR(30))", null);
    io:println("CUSTOMER table create status in first DB:" + ret);
    //Create the table named SALARY in the second database.
    ret = testDB2 -> update("CREATE TABLE SALARY (ID INT, VALUE FLOAT)", null);
    io:println("SALARY table create status in second DB:" + ret);

    boolean transactionSuccess = false;
    //Begins the transaction.
    transaction {
        //This is the first action participate in the transaction which insert customer
        //name to the first DB and get the generated key.
        var insertCount, generatedID = testDB1 -> updateWithGeneratedKeys("INSERT INTO
                                     CUSTOMER(NAME) VALUES ('Anne')", null, null);
        var returnedKey, _ = <int>generatedID[0];
        io:println("Inserted count to CUSTOMER table:" + insertCount);
        io:println("Generated key for the inserted row:" + returnedKey);
        //This is the second action participate in the transaction which insert the
        //salary info to the second DB along with the key generated in the first DB.
        sql:Parameter para1 = {sqlType:sql:Type.INTEGER, value:returnedKey};
        sql:Parameter[] params = [para1];
        ret = testDB2 -> update("INSERT INTO SALARY (ID, VALUE) VALUES (?, 2500)", params);
        io:println("Inserted count to SALARY table:" + ret);

        transactionSuccess = true;
    } onretry {
        io:println("Transaction failed");
        transactionSuccess = false;
    }
    if (transactionSuccess) {
        io:println("Transaction committed");
    }
    //Drop the tables created for this sample.
    ret = testDB1 -> update("DROP TABLE CUSTOMER", null);
    io:println("CUSTOMER table drop status:" + ret);
    ret = testDB2 -> update("DROP TABLE SALARY", null);
    io:println("SALARY table drop status:" + ret);

    //Close the connection pool.
    testDB1 -> close();
    testDB2 -> close();
}
