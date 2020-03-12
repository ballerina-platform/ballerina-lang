import ballerina/io;
import ballerina/mysql;
import ballerina/sql;

// Username and password of MySQL database. This is used in below examples when initializing the
// MySQL connector. Please change these based on your setup if you want to try locally.
string dbUser = "root";
string dbPassword = "Test@123";
string dbName = "MYSQL_BBE_EXEC";

function initializeDatabase() returns sql:Error? {
    // Initialize client without any database to create the database.
    mysql:Client mysqlClient = check new (user = dbUser, password = dbPassword);
    // Create database if it is not exists. If any error occurred the error will be returned.
    sql:ExecuteResult? result = check mysqlClient->execute("CREATE DATABASE IF NOT EXISTS " + dbName);
    io:println("Database created");
    // Close this MySQL client.
    check mysqlClient.close();
}

function initializeTable(mysql:Client mysqlClient) returns int|string|sql:Error? {
    // Execute drop table. The sql:ExecuteResult is returned during the successful execution.
    // And error whill be returned in case of failure.
    sql:ExecuteResult? result = check mysqlClient->execute("DROP TABLE IF EXISTS Customers");
    if (result is sql:ExecuteResult) {
        io:println("Drop table executed");
        io:println(result);
    }
    // Similarly to drop table, the create table query is executed. Here the `customerId`
    // is a auto generated column.
    result = check mysqlClient->execute("CREATE TABLE IF NOT EXISTS Customers(customerId INTEGER " +
        "NOT NULL AUTO_INCREMENT, firstName  VARCHAR(300),lastName  VARCHAR(300), registrationID INTEGER," +
        "creditLimit DOUBLE, country  VARCHAR(300), PRIMARY KEY (customerId))");

    // Insert sample data into the table. The result will have `affectedRowCount` and `lastInsertedId`
    // with the auto generated id of the last row.
    result = check mysqlClient->execute("INSERT INTO Customers (firstName,lastName,registrationID,creditLimit," +
        "country) VALUES ('Peter', 'Stuart', 1, 5000.75, 'USA')");
    int|string? generatedId = ();

    if (result is sql:ExecuteResult) {
        io:print("Rows affected: ");
        io:println(result.affectedRowCount);
        io:print("Generated Customer ID: ");
        io:println(result.lastInsertId);
        generatedId = result.lastInsertId;
    }
    return generatedId;
}

function updateRecord(mysql:Client mysqlClient, int generatedId) {
    // Update the record with the auto generated ID.
    string query = string `Update Customers set creditLimit = 15000.5 where customerId = ${generatedId}`;
    sql:ExecuteResult|sql:Error? result = mysqlClient->execute(query);
    if (result is sql:ExecuteResult) {
        io:print("Updated Row count: ");
        io:println(result?.affectedRowCount);
    } else if (result is sql:Error) {
        io:println("Error occured: ");
        io:println(result);
    } else {
        io:println("Empty result");
    }
}

function deleteRecord(mysql:Client mysqlClient, int generatedId) {
    // Delete the record with the auto generated ID.
    string query = string `Delete from Customers where customerId = ${generatedId}`;
    sql:ExecuteResult|sql:Error? result = mysqlClient->execute(query);
    if (result is sql:ExecuteResult) {
        io:print("Deleted Row count: ");
        io:println(result.affectedRowCount);
    } else if (result is sql:Error) {
        io:println("Error occured: ");
        io:println(result);
    } else {
        io:println("Empty result");
    }
}

public function main() {
    // Initialize the database.
    sql:Error? err = initializeDatabase();
    if (err is ()) {
        // Initialize the MySQL client to be used for the rest of DDL and DML operations.
        mysql:Client|sql:Error mysqlClient = new (user = dbUser, password = dbPassword, database = dbName);
        if (mysqlClient is mysql:Client) {
            //  Initialize table and insert data.
            int|string|sql:Error? initResult = initializeTable(mysqlClient);
            if (initResult is int) {
                // Update a record.
                updateRecord(mysqlClient, initResult);
                // Delete a record.
                deleteRecord(mysqlClient, initResult);
                io:println("Successfully completed!");
            } else if (initResult is sql:Error) {
                io:println("Customer table initialization failed!");
                io:println(initResult);
            }
            // Close the MySQL client.
            sql:Error? e = mysqlClient.close();
        } else {
            io:println("Table initialization failed!!");
            io:println(mysqlClient);
        }
    } else {
        io:println("Database initialization failed!!");
        io:println(err);
    }
}
