import ballerina/io;
import ballerina/sql;
import ballerinax/mysql;

// The username and password of the MySQL database. This is used in the below
// examples when initializing the MySQL connector. You need to change these
// based on your setup if you try locally.
string dbUser = "root";
string dbPassword = "Test@123";
string dbName = "MYSQL_BBE";

function initializeDatabase() returns sql:Error? {
    // Initialize the client without any database to create the database.
    mysql:Client mysqlClient = check new (user = dbUser, password = dbPassword);
    // Create the database if it does not exist. If any error occurred,
    // the error will be returned.
    sql:ExecutionResult result =
        check mysqlClient->execute("CREATE DATABASE IF NOT EXISTS " + dbName);
    io:println("Database created. ");
    // Close the MySQL client.
    check mysqlClient.close();

}

function initializeTable(mysql:Client mysqlClient)
returns sql:Error? {
    // Execute dropping the table. The `sql:ExecutionResult` is returned upon
    // successful execution. An error will be returned in case of a failure.
    sql:ExecutionResult result =
        check mysqlClient->execute("DROP TABLE IF EXISTS Customers");
    io:println("Drop table executed: ", result);

    // Similarly, to drop a table, the `create` table query is executed.
    // Here, the `customerId` is an auto-generated column.
    result = check mysqlClient->execute(
                    "CREATE TABLE IF NOT EXISTS Customers ( " +
                    "customerId INTEGER NOT NULL AUTO_INCREMENT, " +
                    "firstName VARCHAR(300), lastName VARCHAR(300), " +
                    "registrationID INTEGER UNIQUE, creditLimit DOUBLE, " +
                    "country VARCHAR(300), PRIMARY KEY (customerId))");
    io:println("Create table executed: ", result);

}

function insertRecords(mysql:Client mysqlClient) {

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
                            ${data.registrationID}, ${data.creditLimit},
                            ${data.country})`;

    // Insert the records with the auto-generated ID.
    sql:ExecutionResult[]|sql:Error result =
        mysqlClient->batchExecute(insertQueries);

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

function simulateBatchExecuteFailure(mysql:Client mysqlClient) {

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
                            ${data.registrationID}, ${data.creditLimit},
                            ${data.country})`;

    // Transaction block can be used to rollback if any error occurred.
    transaction {
        var result = mysqlClient->batchExecute(insertQueries);
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
    // Initialize the database.
    sql:Error? err = initializeDatabase();

    if (err is ()) {
        // Initialize the MySQL client to be used for the rest of the DDL
        // and DML operations.
        mysql:Client|sql:Error mysqlClient = new (user = dbUser,
            password = dbPassword, database = dbName);

        if (mysqlClient is mysql:Client) {
            // Initialize a table and insert sample data.
            sql:Error? initResult = initializeTable(mysqlClient);

            if (initResult is ()) {
                // Insert a batch of records.
                insertRecords(mysqlClient);
                // Simulate Failure Rollback.
                simulateBatchExecuteFailure(mysqlClient);
                // Check the data.
                checkData(mysqlClient);

                io:println("\nSample executed successfully!");
            } else {
                io:println("Customer table initialization failed: ",
                                                            initResult);
            }
            // Close the MySQL client.
            sql:Error? e = mysqlClient.close();

        } else {
            io:println("Table initialization failed!!", mysqlClient);
        }
    } else {
        io:println("Database initialization failed!!", err);
    }
}

function checkData(mysql:Client mysqlClient) {
     stream<record{}, error> resultStream =
        mysqlClient->query("Select * from Customers");

     io:println("\nData in Customers table:");
     error? e = resultStream.forEach(function(record {} result) {
                 io:println(result.toString());
     });
     if (e is error) {
        io:println("Select data from Customers table failed!");
        io:println(e);
     }
}
