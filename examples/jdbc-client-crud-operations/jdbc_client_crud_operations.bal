import ballerina/io;
import ballerina/jsonutils;
import ballerinax/java.jdbc;

// JDBC Client for MySQL database. This client can be used with any JDBC
// supported database by providing the corresponding JDBC URL.
jdbc:Client testDB = new({
    url: "jdbc:mysql://localhost:3306/testdb",
    username: "test",
    password: "test",
    poolOptions: { maximumPoolSize: 5 },
    dbOptions: { useSSL: false }
});

// This is the `type` created to represent a data row.
type Student record {
    int id;
    int age;
    string name;
};

public function main() {
    // Create a table using the `update` remote function. If the DDL
    // statement execution is successful, the `update` remote function
    // returns 0.
    io:println("The update operation - Creating a table");
    var ret = testDB->update("CREATE TABLE student(id INT AUTO_INCREMENT, " +
                         "age INT, name VARCHAR(255), PRIMARY KEY (id))");
    handleUpdate(ret, "Create student table");

    // Insert data to the table using the `update` remote function. If the DML
    // statement execution is successful, the `update` remote function returns
    // the updated row count. The query parameters are given in the query
    // statement itself.
    io:println("\nThe update operation - Inserting data to a table");
    ret = testDB->update("INSERT INTO student(age, name) values " +
                          "(23, 'john')");
    handleUpdate(ret, "Insert to student table with no parameters");

    // The query parameters are given as variables for the `update` remote
    // function. Only `int`, `float`, `boolean`, and `string` values are
    // supported as direct variables.
    int age = 24;
    string name = "Anne";
    ret = testDB->update("INSERT INTO student(age, name) values (?, ?)",
        age, name);
    handleUpdate(ret, "Insert to student table with variable parameters");

    // The query parameters are given as arguments of the type `jdbc:Parameter`
    // for the `update` remote function.
    // Default direction is IN.
    jdbc:Parameter p1 = { sqlType: jdbc:TYPE_INTEGER, value: 25 };
    jdbc:Parameter p2 = { sqlType: jdbc:TYPE_VARCHAR, value: "James" };
    ret = testDB->update("INSERT INTO student(age, name) values (?, ?)",
        p1, p2);
    handleUpdate(ret, "Insert to student table with jdbc:parameter values");


    // Update data in the table using the `update` remote function.
    io:println("\nThe Update operation - Update data in a table");
    ret = testDB->update("UPDATE student SET name = 'Jones' WHERE age = ?",
        23);
    handleUpdate(ret, "Update a row in student table");

    // Delete data in a table using the `update` remote function.
    io:println("\nThe Update operation - Delete data from table");
    ret = testDB->update("DELETE FROM student WHERE age = ?", 24);
    handleUpdate(ret, "Delete a row from student table");

    // The column values generated during the update can be retrieved using the
    // `update` remote function. If the table has several auto-generated
    // columns other than the auto-incremented key, those column names
    // should be given as an array. The values of the auto-incremented
    // column and the auto-generated columns are returned as a `string` array.
    // Similar to the `update` remote function, the inserted row count is also
    // returned.
    io:println("\nThe Update operation - Inserting data");
    age = 31;
    name = "Kate";
    var retWithKey = testDB->update("INSERT INTO student " +
                        "(age, name) values (?, ?)", age, name);
    if (retWithKey is jdbc:UpdateResult) {
        int count = retWithKey.updatedRowCount;
        int generatedKey = <int>retWithKey.generatedKeys["GENERATED_KEY"];
        io:println("Inserted row count: ", count);
        io:println("Generated key: ", generatedKey);
    } else {
        error err = retWithKey;
        io:println("Insert failed: ", <string> err.detail()["message"]);
    }

    // Select data using the `select` remote function. The `select` remote
    // function returns a `table`. See the `table` ballerina example for
    // more details on how to access data.
    io:println("\nThe select operation - Select data from a table");
    var selectRet = testDB->select("SELECT * FROM student", Student);
    table<Student> dt;
    if (selectRet is table<Student>) {
        // `table` can be converted to either `json` or `xml`. The actual
        // conversion happens on-demand. When a service client makes a request,
        // the result is streamed to the service instead of building the full
        // result in the server and returning it. This allows unlimited payload
        // sizes in the result and the response is instantaneous to the client.
        // Convert a table to `json`.
        json jsonConversionRet = jsonutils:fromTable(selectRet);
        io:print("JSON: ");
        io:println(jsonConversionRet.toJsonString());
    } else {
        error err = selectRet;
        io:println("Select data from student table failed: ",
                <string> err.detail()["message"]);
    }
    // Drop the table and procedures.
    io:println("\nThe update operation - Drop the student table");
    ret = testDB->update("DROP TABLE student");
    handleUpdate(ret, "Drop table student");
}

// Function to handle the return value of the `update` remote function.
function handleUpdate(jdbc:UpdateResult|jdbc:Error returned, string message) {
    if (returned is jdbc:UpdateResult) {
        io:println(message, " status: ", returned.updatedRowCount);
    } else {
        error err = returned;
        io:println(message, " failed: ", <string> err.detail()["message"]);
    }
}
