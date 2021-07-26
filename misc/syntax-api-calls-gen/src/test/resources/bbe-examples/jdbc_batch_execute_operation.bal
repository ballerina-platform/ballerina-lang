import ballerina/io;
import ballerina/java.jdbc;
import ballerina/sql;

function initializeTable(jdbc:Client jdbcClient)
returns sql:Error? {
    // Execute dropping the table. The `sql:ExecutionResult` is returned upon
    // successful execution. An error will be returned in case of a failure.
    sql:ExecutionResult result =
        check jdbcClient->execute("DROP TABLE IF EXISTS Customers");
    io:println("Drop table executed: ", result);

    // Similarly, to drop a table, the `create` table query is executed.
    // Here, the `customerId` is an auto-generated column.
    result = check jdbcClient->execute("CREATE TABLE IF NOT EXISTS Customers" +
        "(customerId INTEGER NOT NULL IDENTITY, firstName VARCHAR(300), " +
        "lastName VARCHAR(300), registrationID INTEGER UNIQUE, " + 
        "creditLimit DOUBLE, country VARCHAR(300), PRIMARY KEY (customerId))");
    io:println("Create table executed: ", result);

}

function insertRecords(jdbc:Client jdbcClient) {

    // Records to be inserted.
    var insertRecords = [
        {firstName: "Peter", lastName: "Stuart", registrationID: 1,
                                    creditLimit: 5000.75, country: "USA"},
        {firstName: "Stephanie", lastName: "Mike", registrationID: 2,
                                    creditLimit: 8000.00, country: "USA"},
        {firstName: "Bill", lastName: "John", registrationID: 3,
                                    creditLimit: 3000.25, country: "USA"}
    ];

    // Create a batch Parameterized Query.
    sql:ParameterizedQuery[] insertQueries =
        from var data in insertRecords
            select  `INSERT INTO Customers
                (firstName, lastName, registrationID, creditLimit, country)
                VALUES (${data.firstName}, ${data.lastName},
                ${data.registrationID}, ${data.creditLimit}, ${data.country})`;
    
    // Insert the records with the auto-generated ID.
    sql:ExecutionResult[]|sql:Error result =
        jdbcClient->batchExecute(insertQueries);

    if (result is sql:ExecutionResult[]) {
        int[] generatedIds = [];
        foreach var summary in result {
            generatedIds.push(<int> summary.lastInsertId);
        }
        io:println("\nInsert success, generated IDs are: ", generatedIds, "\n");
    } else {
        io:println("Error occurred: ", result);
    }
}

function simulateBatchExecuteFailure(jdbc:Client jdbcClient) {

    // Records with duplicate `registrationID` entry (registrationID = 1).
    var insertRecords = [
        {firstName: "Linda", lastName: "Jones", registrationID: 4,
                                    creditLimit: 10000.75, country: "USA"},
        {firstName: "Peter", lastName: "Stuart", registrationID: 1,
                                    creditLimit: 5000.75, country: "USA"},
        {firstName: "Camellia", lastName: "Potter", registrationID: 5,
                                    creditLimit: 2000.25, country: "USA"}
    ];

    // Create a batch Parameterized Query.
    sql:ParameterizedQuery[] insertQueries =
        from var data in insertRecords
            select  `INSERT INTO Customers
                (firstName, lastName, registrationID, creditLimit, country)
                VALUES (${data.firstName}, ${data.lastName},
                ${data.registrationID}, ${data.creditLimit}, ${data.country})`;

    // Transaction block can be used to rollback if any error occurred.
    transaction {
        var result = jdbcClient->batchExecute(insertQueries);
        if (result is sql:BatchExecuteError) {
            io:println(result.message());
            io:println(result.detail()?.executionResults);
            io:println("Rollback transaction.");
            rollback;
        } else {
            error? err = commit;
            io:println("Error occurred while committing: ", err);
        }
    }

}

public function main() {
    // Initialize the JDBC client.
    jdbc:Client|sql:Error jdbcClient = new ("jdbc:h2:file:./target/customers",
        "rootUser", "rootPass");

    if (jdbcClient is jdbc:Client) {
        // Initialize a table and insert sample data.
        sql:Error? initResult = initializeTable(jdbcClient);

        if (initResult is ()) {
            // Insert a batch of records.
            insertRecords(jdbcClient);
            // Simulate Failure Rollback.
            simulateBatchExecuteFailure(jdbcClient);
            // Check the data.
            checkData(jdbcClient);

            io:println("\nSample executed successfully!");
        } else {
            io:println("Customer table initialization failed: ", initResult);
        }
        // Close the JDBC client.
        sql:Error? e = jdbcClient.close();

    } else {
        io:println("Initialization failed!!");
        io:println(jdbcClient);
    }
}

function checkData(jdbc:Client jdbcClient) {
     stream<record{}, error?> resultStream =
        jdbcClient->query("Select * from Customers");

     io:println("\nData in Customers table:");
     error? e = resultStream.forEach(function(record {} result) {
                 io:println(result.toString());
     });
     if (e is error) {
        io:println("Select data from Customers table failed!");
        io:println(e);
     }
}
