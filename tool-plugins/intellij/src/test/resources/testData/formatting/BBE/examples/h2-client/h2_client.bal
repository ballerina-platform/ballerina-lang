import ballerina/io;
import ballerina/h2;

// Create an endpoint for h2 database. Change the path before running the sample.
endpoint h2:Client testDB {
    path: "./h2-client",
    name: "testdb",
    username: "SA",
    password: "",
    poolOptions: { maximumPoolSize: 5 }
};

function main(string... args) {
    // Creates a table using the update operation.
    io:println("The update operation - Creating a table:");
    var ret = testDB->update("CREATE TABLE STUDENT(ID INTEGER,
                    AGE INTEGER, NAME VARCHAR(255), PRIMARY KEY (ID))");
    handleUpdate(ret, "Create student table");

    // Inserts data to the table using the update operation.
    io:println("\nThe update operation - Inserting data to a table");
    ret = testDB->update("INSERT INTO student(id, age, name)
                          values (1, 23, 'john')");
    handleUpdate(ret, "Insert to student table with no parameters");

    // Select data using the `select` operation.
    io:println("\nThe select operation - Select data from a table");
    var selectRet = testDB->select("SELECT * FROM student", ());
    table dt;
    match selectRet {
        table tableReturned => dt = tableReturned;
        error e => io:println("Select data from student table failed: "
                              + e.message);
    }
    // Convert a table to JSON.
    io:println("\nConvert the table into json");
    var jsonConversionRet = <json>dt;
    match jsonConversionRet {
        json jsonRes => {
            io:print("JSON: ");
            io:println(io:sprintf("%s", jsonRes));
        }
        error e => io:println("Error in table to json conversion");
    }

    // Drop the STUDENT table.
    io:println("\nThe update operation - Drop student table");
    ret = testDB->update("DROP TABLE student");
    handleUpdate(ret, "Drop table student");

    // Finally, close the connection pool.
    testDB.stop();
}

// Function to handle return of the update operation.
function handleUpdate(int|error returned, string message) {
    match returned {
        int retInt => io:println(message + " status: " + retInt);
        error e => io:println(message + " failed: " + e.message);
    }
}
