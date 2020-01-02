import ballerina/io;
import ballerinax/java.jdbc;

// JDBC Client for the first H2 database. Since this `Client`
// participates in a distributed transaction, the `isXA` property should be true.
jdbc:Client testDB1 = new ({
    url: "jdbc:h2:file:./xa-transactions/Testdb1",
    username: "test",
    password: "test",
    poolOptions: {isXA: true}
});

// JDBC Client for the second H2 database. Since this endpoint
// participates in a distributed transaction, the `isXA` property should be true.
jdbc:Client testDB2 = new ({
    url: "jdbc:h2:file:./xa-transactions/Testdb2",
    username: "test",
    password: "test",
    poolOptions: {isXA: true}
});

public function main() {
    // Create the table named CUSTOMER in the first database.
    var ret = testDB1->update("CREATE TABLE CUSTOMER (ID INTEGER " +
                              "AUTO_INCREMENT, NAME VARCHAR(30))");
    handleUpdate(ret, "Create CUSTOMER table");
    // Create the table named SALARY in the second database.
    ret = testDB2->update("CREATE TABLE SALARY (ID INT, VALUE FLOAT)");
    handleUpdate(ret, "Create SALARY table");

    // Start the transaction.
    transaction {
        // This is the first remote function to participate in the transaction. It inserts
        // the customer name to the first DB and gets the generated key.
        ret = testDB1->update("INSERT INTO CUSTOMER(NAME) " +
                               "VALUES ('Anne')");
        int key = -1;
        if (ret is jdbc:UpdateResult) {
            int count = ret.updatedRowCount;
            key = <int>ret.generatedKeys["ID"];
            io:println("Inserted row count: ", count);
            io:println("Generated key: ", key);
        } else {
            io:println("Insert to student table failed: ",
                        <string>ret.detail()?.message);
        }

        // This is the second remote function to participate in the transaction. It inserts the
        // salary info to the second DB along with the key generated in the first DB.
        ret = testDB2->update("INSERT INTO SALARY (ID, VALUE) VALUES (?, ?)",
                                    key, 2500);
        handleUpdate(ret, "Insert to SALARY table");
    } onretry {
        io:println("Retrying transaction");
    } committed {
        io:println("Transaction committed");
    } aborted {
        io:println("Transaction aborted");
    }

    // Drop the tables created for this sample.
    ret = testDB1->update("DROP TABLE CUSTOMER");
    handleUpdate(ret, "Drop Table CUSTOMER");

    ret = testDB2->update("DROP TABLE SALARY");
    handleUpdate(ret, "Drop Table SALARY");
}

// Function to handle the return value of the `update` remote function.
function handleUpdate(jdbc:UpdateResult|error returned, string message) {
    if (returned is jdbc:UpdateResult) {
        io:println(message, " status: ", returned.updatedRowCount);
    } else {
        io:println(message, " failed: ", <string>returned.detail()?.message);
    }
}
