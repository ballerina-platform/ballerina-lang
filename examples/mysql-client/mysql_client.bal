import ballerina/io;
import ballerina/mysql;

// Create a client endpoint for MySQL database. Change the DB details before running the sample.
mysql:Client testDB = new({
        host: "localhost",
        port: 3306,
        name: "testdb",
        username: "test",
        password: "test",
        poolOptions: { maximumPoolSize: 5 },
        dbOptions: { useSSL: false }
    });

public function main() {

    // Create a table using the `update` remote function.
    io:println("The update operation - Creating a table:");
    var ret = testDB->update("CREATE TABLE student(id INT AUTO_INCREMENT,
                          age INT, name VARCHAR(255), PRIMARY KEY (id))");
    handleUpdate(ret, "Create student table");

    // Insert data to the table using the update remote function.
    io:println("\nThe update operation - Inserting data to a table");
    ret = testDB->update("INSERT INTO student(age, name)
                          values (23, 'john')");
    handleUpdate(ret, "Insert to student table with no parameters");

    // Select data using the `select` remote function.
    io:println("\nThe select operation - Select data from a table");
    var selectRet = testDB->select("SELECT * FROM student", ());

    if (selectRet is table<record {}>) {
        // Convert a `table` to `json`.
        io:println("\nConvert the table into json");
        var jsonConversionRet = json.convert(selectRet);
        if (jsonConversionRet is json) {
            io:print("JSON: ");
            io:println(io:sprintf("%s", jsonConversionRet));
        } else {
            io:println("Error in table to json conversion");
        }
    } else {
        io:println("Select data from student table failed: "
                + <string>selectRet.detail().message);
    }

    // Drop the STUDENT table.
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
