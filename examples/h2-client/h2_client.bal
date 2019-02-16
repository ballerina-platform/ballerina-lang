import ballerina/h2;
import ballerina/io;

// Create a client endpoint for the `h2` database. Before running the sample,
// change the value of the 'path' field 
// to indicate the path of a directory you create in a preferred location.
// This will create a new database in the given path if one does not exist already.
h2:Client testDB = new({
        path: "./h2-client",
        name: "testdb",
        username: "SA",
        password: "",
        poolOptions: { maximumPoolSize: 5 }
    });

public function main() {
    // Create a table using the `update` remote function.
    io:println("The update operation - Creating a table:");
    var ret = testDB->update("CREATE TABLE STUDENT(ID INTEGER,
                    AGE INTEGER, NAME VARCHAR(255), PRIMARY KEY (ID))");
    handleUpdate(ret, "Create student table");

    // Insert data to the table using the `update` remote function.
    io:println("\nThe update operation - Inserting data to a table");
    ret = testDB->update("INSERT INTO student(id, age, name)
                          values (1, 23, 'john')");
    handleUpdate(ret, "Insert to student table with no parameters");

    // Select data using the `select` remote function.
    io:println("\nThe select operation - Select data from a table");
    var selectRet = testDB->select("SELECT * FROM student", ());
    if (selectRet is table<record {}>) {
        // Convert a `table` to `json`.
        io:println("\nConvert the table into json");
        var jsonConversionRet = json.convert(selectRet);
        if (jsonConversionRet is json) {
            io:println("JSON: ", io:sprintf("%s", jsonConversionRet));
        } else {
            io:println("Error in table to json conversion");
        }
    } else {
        io:println("Select data from student table failed: "
                     + <string>selectRet.detail().message);
    }

    // Drop the STUDENT table in the database.
    io:println("\nThe update operation - Drop student table");
    ret = testDB->update("DROP TABLE student");
    handleUpdate(ret, "Drop table student");
}

// Function to handle return value of the `update` remote function.
function handleUpdate(int|error returned, string message) {
    if (returned is int) {
        io:println(message + " status: " + returned);
    } else {
        io:println(message + " failed: " + <string>returned.detail().message);
    }
}
