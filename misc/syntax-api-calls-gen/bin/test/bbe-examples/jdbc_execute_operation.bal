import ballerina/io;
import ballerina/java.jdbc;
import ballerina/sql;

function initializeTable(jdbc:Client jdbcClient)
returns int|string|sql:Error? {
    // Execute dropping the table. The `sql:ExecutionResult` is returned upon
    // successful execution. An error will be returned in case of a failure.
    sql:ExecutionResult result =
        check jdbcClient->execute("DROP TABLE IF EXISTS Customers");
    io:println("Drop table executed. ", result);

    // Similarly, to drop a table, the `create` table query is executed.
    // Here, the `customerId` is an auto-generated column.
    result = check jdbcClient->execute("CREATE TABLE IF NOT EXISTS Customers" +
        "(customerId INTEGER NOT NULL IDENTITY, firstName VARCHAR(300), " +
        "lastName VARCHAR(300), registrationID INTEGER, creditLimit DOUBLE, " +
        "country VARCHAR(300), PRIMARY KEY (customerId))");

    // Insert sample data into the table. The result will have
    // `affectedRowCount` and `lastInsertedId` with the auto-generated ID of
    // the last row.
    result = check jdbcClient->execute("INSERT INTO Customers (firstName, " +
        "lastName,registrationID,creditLimit,country)" +
        "VALUES ('Peter', 'Stuart', 1, 5000.75, 'USA')");
    io:println("Rows affected: ", result.affectedRowCount);
    io:println("Generated Customer ID: ", result.lastInsertId);
    
    return result.lastInsertId;
}

function updateRecord(jdbc:Client jdbcClient, int generatedId) {
    // Create a parameterized query.
    sql:ParameterizedQuery updateQuery =
        `Update Customers set creditLimit = 15000.5
         where customerId = ${generatedId}`;

    // Update the record with the auto-generated ID.
    sql:ExecutionResult|sql:Error result =
        jdbcClient->execute(updateQuery);

    if (result is sql:ExecutionResult) {
        io:println("Updated Row count: ", result?.affectedRowCount);
    } else {
        io:println("Error occurred: ", result);
    }
}

function deleteRecord(jdbc:Client jdbcClient, int generatedId) {
    // Delete the record with the auto-generated ID.
    sql:ParameterizedQuery deleteQuery =
        `Delete from Customers where customerId = ${generatedId}`;
    sql:ExecutionResult|sql:Error result =
            jdbcClient->execute(deleteQuery);

    if (result is sql:ExecutionResult) {
        io:println("Deleted Row count: ", result.affectedRowCount);
    } else {
        io:println("Error occurred: ", result);
    }
}

public function main() {
    // Initialize the JDBC client.
    jdbc:Client|sql:Error jdbcClient = new ("jdbc:h2:file:./target/customers",
        "rootUser", "rootPass");

    if (jdbcClient is jdbc:Client) {
        // Initialize a table and insert sample data.
        int|string|sql:Error? initResult = initializeTable(jdbcClient);

        if (initResult is int) {
            // Update a record.
            updateRecord(jdbcClient, initResult);
            // Delete a record.
            deleteRecord(jdbcClient, initResult);

            io:println("Sample executed successfully!");
        } else if (initResult is sql:Error) {
            io:println("Customer table initialization failed: ", initResult);
        }
        // Close the JDBC client.
        sql:Error? e = jdbcClient.close();

    } else {
        io:println("Initialization failed!!");
        io:println(jdbcClient);
    }
}
