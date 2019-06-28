import ballerina/io;
import ballerina/mysql;
import ballerina/sql;

// Creates a client endpoint for the MySQL database. Change the DB details before running the sample.
mysql:Client testDB = new({
        host: "localhost",
        port: 3306,
        name: "testdb",
        username: "test",
        password: "test",
        dbOptions: { useSSL: false }
    });

public function main() {

    // Creates a table using the `update` remote function.
    io:println("The update operation - Creating a table:");
    var ret = testDB->update("CREATE TABLE student(id INT AUTO_INCREMENT,
                          age INT, name VARCHAR(255), PRIMARY KEY (id))");
    handleUpdate(ret, "Create student table");

    // Inserts data to the table using the update remote function.
    io:println("\nThe update operation - Inserting data to a table");
    ret = testDB->update("INSERT INTO student(age, name)
                          values (23, 'john')");
    handleUpdate(ret, "Insert to student table with no parameters");

    // Selects data using the `select` remote function.
    io:println("\nThe select operation - Select data from a table");
    var selectRet = testDB->select("SELECT * FROM student", ());

    if (selectRet is table<record {}>) {
        // Converts a `table` to `json`.
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

    // Drops the STUDENT table from the database.
    io:println("\nThe update operation - Drop student table");
    ret = testDB->update("DROP TABLE student");
    handleUpdate(ret, "Drop table student");

    // Stops the database client.
    var stopRet = testDB.stop();
    if (stopRet is error) {
        io:println(stopRet.detail().message);
    }
}

// This function handles the return value of the `update` remote function.
function handleUpdate(sql:UpdateResult|error returned, string message) {
    if (returned is sql:UpdateResult) {
        io:println(message + " status: " + returned.updatedRowCount);
    } else {
        io:println(message + " failed: " + <string>returned.detail().message);
    }
}
