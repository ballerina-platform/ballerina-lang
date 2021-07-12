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
    mysql:Client sqlClient = check new (user = dbUser, password = dbPassword);
    // Create the database if it does not exist. If any error occurred,
    // the error will be returned.
    sql:ExecutionResult result =
        check sqlClient->execute("CREATE DATABASE IF NOT EXISTS " + dbName);
    io:println("Database created. ");
    // Close the MySQL client.
    check sqlClient.close();

}

function initializeTable(mysql:Client sqlClient)
returns sql:Error? {

    io:println("Creating necessary table and procedures...");
    // Execute dropping the table. The `sql:ExecutionResult` is returned upon
    // successful execution. An error will be returned in case of a failure.
    sql:ExecutionResult result =
        check sqlClient->execute("DROP TABLE IF EXISTS Student");
    result = check sqlClient->execute(
                                "DROP PROCEDURE IF EXISTS InsertStudent");
    result = check sqlClient->execute("DROP PROCEDURE IF EXISTS GetCount");
    result = check sqlClient->execute("DROP PROCEDURE IF EXISTS GetStudents");
    io:println("Successfully dropped tables and procedures.");
    
    // Similarly, to drop a table, the `create` table query is executed.
    // Here, the `id` is an auto-generated column.
    result =  check sqlClient->execute("CREATE TABLE Student(" +
            " id INT AUTO_INCREMENT, age INT, name VARCHAR(255), " + 
            " PRIMARY KEY (id))");
    io:println("Create Student table executed.");

    // Necessary stored procedures are created using the execute command.
    result = check sqlClient->execute("CREATE PROCEDURE InsertStudent" + 
        "(IN pName VARCHAR(255), IN pAge INT) " +
        "BEGIN " +
        "INSERT INTO Student(age, name) VALUES (pAge, pName); " +
        "END");
    io:println("Stored procedure with IN param created.");

    result = check sqlClient->execute("CREATE PROCEDURE GetCount" + 
        "(INOUT pID INT, OUT totalCount INT) " +
        "BEGIN " +
        "SELECT age INTO pID FROM Student WHERE id = pID; " +
        "SELECT COUNT(*) INTO totalCount FROM Student;" +
        "END");
    io:println("Stored procedure with INOUT/OUT param created.");

    result = check sqlClient->execute("CREATE PROCEDURE GetStudents() " +
        "BEGIN SELECT * FROM Student; END");
    io:println("Stored procedure with result set returned created.");
}

// The stored procedure `InsertStudent` with the IN parameters is invoked. 
function insertRecord(mysql:Client sqlClient) {

    io:println("\nInvoke `InsertStudent` procedure with IN params");

    // Create a parameterized query to invoke the procedure.
    string name = "George";
    int age = 24;
    sql:ParameterizedCallQuery sqlQuery = 
                                `CALL InsertStudent(${name}, ${age})`;

    // Execute the stored procedure.
    sql:ProcedureCallResult|sql:Error retCall = sqlClient->call(sqlQuery);

    if (retCall is sql:ProcedureCallResult) {
        io:println("Call stored procedure `InsertStudent` is successful : ", 
                retCall.executionResult);
    } else {
        io:println("Error occurred while invoking `InsertStudent`.");
    }
}

// Here, the stored procedure with the OUT and INOUT parameters is invoked.
function getCount(mysql:Client sqlClient) {

    io:println("\nInvoke `GetCount` procedure with INOUT & OUT params");

    // Initialize the INOUT & OUT parameters.
    sql:InOutParameter id = new (1);
    sql:IntegerOutParameter totalCount = new;
    sql:ParameterizedCallQuery sqlQuery = 
                        `{CALL GetCount(${id}, ${totalCount})}`;

    // Execute the stored procedure.
    sql:ProcedureCallResult|sql:Error retCall = sqlClient->call(sqlQuery);

    if (retCall is sql:ProcedureCallResult) {
        io:println("Call stored procedure `GetCount` is successful.");
        io:println("Age of the student with id '1' : ", id.get(int));
        io:println("Total student count: ", totalCount.get(int));
    } else {
        io:println("Error occurred while invoking `GetCount`.");
    }

}

// Student record to represent the database table.
type Student record {
    int id;
    int age;
    string name;
};

// Invoke the stored procedure, which returns data.
function checkData(mysql:Client sqlClient) {

    io:println("\nInvoke `GetStudents` procedure with returned data");

    // Execute the stored procedure.
    sql:ProcedureCallResult|sql:Error retCall = 
                            sqlClient->call("{CALL GetStudents()}", [Student]);

    if (retCall is sql:ProcedureCallResult) {
        io:println("Call stored procedure `InsertStudent` is successful.");
        // Process the returned result stream.
        stream<record{}, sql:Error?>? result = retCall.queryResult;
        if (!(result is ())) {
            stream<Student, sql:Error?> studentStream =
                                    <stream<Student, sql:Error?>> result;
            sql:Error? e = studentStream.forEach(function(Student student) {
                                io:println("Student details: ", student);
                           });
        } else {
            io:println("Empty result is returned from the `GetStudents`.");
        }

    } else {
        io:println("Error occurred while invoking `GetStudents`.");
    }

}

public function main() {
    // Initialize the database.
    sql:Error? err = initializeDatabase();

    if (err is ()) {
        // Initialize the MySQL client to be used for the rest of the 
        // procedure call executions.
        mysql:Client|sql:Error sqlClient = new (user = dbUser,
            password = dbPassword, database = dbName);

        if (sqlClient is mysql:Client) {
            // Initialize a table and insert sample data.
            sql:Error? initResult = initializeTable(sqlClient);

            if (initResult is ()) {
                // Insert a record by calling a procedure.
                insertRecord(sqlClient);
                // Get the total count by calling a procedure.
                getCount(sqlClient);
                // Get all the results by invoking a procedure.
                checkData(sqlClient);

                io:println("\nSample executed successfully!");
            } else {
                io:println("Student table initialization failed: ", initResult);
            }
            // Close the MySQL client.
            sql:Error? e = sqlClient.close();

        } else {
            io:println("Table initialization failed!!", sqlClient);
        }
    } else {
        io:println("Database initialization failed!!", err);
    }
}
