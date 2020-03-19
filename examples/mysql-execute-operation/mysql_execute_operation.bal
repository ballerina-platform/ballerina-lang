import ballerina/io;
import ballerina/mysql;
import ballerina/sql;

// Username and password of the MySQL database. This is used in the below
// examples when initializing the MySQL connector. You need to change these
// based on your setup if you try locally.
string dbUser = "root";
string dbPassword = "Test@123";
string dbName = "MYSQL_BBE_EXEC";

function initializeDatabase() returns sql:Error? {
    // Initialize the client without any database to create the database.
    mysql:Client mysqlClient = check new (user = dbUser, password = dbPassword);
    // Create database if it does not exist. If any error occurred,
    // the error will be returned.
    sql:ExecuteResult? result =
        check mysqlClient->execute("CREATE DATABASE IF NOT EXISTS " + dbName);
    io:println("Database created. ");
    // Close the MySQL client.
    check mysqlClient.close();
}

function initializeTable(mysql:Client mysqlClient)
    returns int|string|sql:Error? {
    // Execute dropping the table. The `sql:ExecuteResult` is returned upon
    // successful execution. An error will be returned in case of a failure.
    sql:ExecuteResult? result =
        check mysqlClient->execute("DROP TABLE IF EXISTS Customers");
    if (result is sql:ExecuteResult) {
        io:println("Drop table executed. ", result);
    }
    // Similarly, to drop a table, the `create` table query is executed.
    // Here, the `customerId` is an auto-generated column.
    result = check mysqlClient->execute("CREATE TABLE IF NOT EXISTS Customers" +
        "(customerId INTEGER NOT NULL AUTO_INCREMENT, firstName  VARCHAR(300)" +
        ",lastName  VARCHAR(300), registrationID INTEGER," +
        "creditLimit DOUBLE, country  VARCHAR(300), PRIMARY KEY (customerId))");

    // Insert sample data into the table. The result will have
    // `affectedRowCount` and `lastInsertedId` with the auto-generated ID of
    // the last row.
    result = check mysqlClient->execute("INSERT INTO Customers (firstName," +
        "lastName,registrationID,creditLimit, country) VALUES ('Peter', " +
        "'Stuart', 1, 5000.75, 'USA')");
    int|string? generatedId = ();

    if (result is sql:ExecuteResult) {
        io:println("Rows affected: ", result.affectedRowCount);
        io:println("Generated Customer ID: ", result.lastInsertId);
        generatedId = result.lastInsertId;
    }
    return generatedId;
}

function updateRecord(mysql:Client mysqlClient, int generatedId) {
    // Update the record with the auto-generated ID.
    string query = string ` ${generatedId}`;
    sql:ExecuteResult|sql:Error? result =
        mysqlClient->execute("Update Customers set creditLimit = 15000.5 "+
        "where customerId =" + generatedId.toString());
    if (result is sql:ExecuteResult) {
        io:println("Updated Row count: ", result?.affectedRowCount);
    } else if (result is sql:Error) {
        io:println("Error occured: ", result);
    } else {
        io:println("Empty result");
    }
}

function deleteRecord(mysql:Client mysqlClient, int generatedId) {
    // Delete the record with the auto-generated ID.
    sql:ExecuteResult|sql:Error? result =
        mysqlClient->execute("Delete from Customers where customerId = "+
        generatedId.toString());
    if (result is sql:ExecuteResult) {
        io:println("Deleted Row count: ", result.affectedRowCount);
    } else if (result is sql:Error) {
        io:println("Error occured: ", result);
    } else {
        io:println("Empty result");
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
            //  Initialize a table and insert data.
            int|string|sql:Error? initResult = initializeTable(mysqlClient);
            if (initResult is int) {
                // Update a record.
                updateRecord(mysqlClient, initResult);
                // Delete a record.
                deleteRecord(mysqlClient, initResult);
                io:println("Sample executed successfully!");
            } else if (initResult is sql:Error) {
                io:println("Customer table initialization failed!", initResult);
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
