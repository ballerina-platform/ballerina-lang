import ballerina/io;
import ballerinax/java.jdbc;

// Client for MySQL database. This client can be used with any JDBC
// supported database by providing the corresponding JDBC URL.
jdbc:Client testDB = new ({
    url: "jdbc:mysql://localhost:3306/testdb",
    username: "test",
    password: "test",
    dbOptions: {useSSL: false}
});

// This is the `type` created to represent a data row.
type Student record {
    int id;
    int age;
    string name;
};

public function main() {
    // Create a table using the `update` remote function.
    io:println("The update operation - Creating table and procedures:");
    var ret = testDB->update("CREATE TABLE student(id INT AUTO_INCREMENT, " +
        "age INT, name VARCHAR(255), PRIMARY KEY (id))");
    handleUpdate(ret, "Create student table");

    // Create the stored procedure with IN parameters.
    ret = testDB->update("CREATE PROCEDURE INSERTDATA(IN pAge INT, " +
        "IN pName VARCHAR(255)) " +
        "BEGIN " +
        "INSERT INTO student(age, name) VALUES (pAge, pName); " +
        "END");
    handleUpdate(ret, "Stored procedure with IN param creation");

    // Create the stored procedure with INOUT and OUT parameters.
    ret = testDB->update("CREATE PROCEDURE GETCOUNT (INOUT pID INT, " +
        "OUT pCount INT) " +
        "BEGIN " +
        "SELECT id INTO pID FROM student WHERE age = pID; " +
        "SELECT COUNT(*) INTO pCount FROM student " +
        "WHERE age = 20; " +
        "END");
    handleUpdate(ret, "Stored procedure with INOUT/OUT param creation");

    ret = testDB->update("CREATE PROCEDURE GETSTUDENTS() " +
        "BEGIN SELECT * FROM student; END");
    handleUpdate(ret, "Stored procedure with result set return");

    // The remote function `call` is used to invoke a stored procedure.
    // Here the stored procedure with IN parameters is invoked.
    io:println("\nThe call operation - With IN params");
    // Invoke the stored procedure with IN type parameters.
    var retCall = testDB->call("{CALL INSERTDATA(?,?)}", (), 20, "George");
    if (retCall is error) {
        io:println("Stored procedure call failed: ",
                    <string>retCall.detail()?.message);
    } else {
        io:println("Call operation with IN params successful");
    }

    // Here stored procedure with OUT and INOUT parameters is invoked.
    io:println("\nThe call operation - With INOUT/OUT params");
    // Define the parameters for INOUT arguments.
    jdbc:Parameter pId = {
        sqlType: jdbc:TYPE_INTEGER,
        value: 20,
        direction: jdbc:DIRECTION_INOUT
    };
    jdbc:Parameter pCount = {
        sqlType: jdbc:TYPE_INTEGER,
        direction: jdbc:DIRECTION_OUT
    };
    // Invoke the stored procedure.
    retCall = testDB->call("{CALL GETCOUNT(?,?)}", (), pId, pCount);
    if (retCall is error) {
        io:println("Stored procedure call failed: ",
                    <string>retCall.detail()?.message);
    } else {
        io:println("Call operation with INOUT and OUT params successful");
        io:println("Student ID of the student with age of 20: ", pId.value);
        io:println("Student count with age of 20: ", pCount.value);
    }

    // Invoke the stored procedure which returns data.
    retCall = testDB->call("{CALL GETSTUDENTS()}", [Student]);
    if (retCall is error) {
        io:println("Stored procedure call failed: ",
                    <string>retCall.detail()?.message);

    } else if retCall is table<record {}>[] {
        table<Student> studentTable = retCall[0];
        io:println("Data in students table:");
        foreach var row in studentTable {
            io:println(row);
        }
    } else {
        io:println("Call operation is not returning data");
    }

    // Drop the table and procedures.
    io:println("\nThe update operation - Drop the tables and procedures");
    ret = testDB->update("DROP TABLE student");
    handleUpdate(ret, "Drop table student");

    ret = testDB->update("DROP PROCEDURE INSERTDATA");
    handleUpdate(ret, "Drop stored procedure INSERTDATA");

    ret = testDB->update("DROP PROCEDURE GETCOUNT");
    handleUpdate(ret, "Drop stored procedure GETCOUNT");

    ret = testDB->update("DROP PROCEDURE GETSTUDENTS");
    handleUpdate(ret, "Drop stored procedure GETSTUDENTS");
}

// Function to handle the return value of the `update` remote function.
function handleUpdate(jdbc:UpdateResult|jdbc:Error returned, string message) {
    if (returned is jdbc:UpdateResult) {
        io:println(message, " status: ", returned.updatedRowCount);
    } else {
        io:println(message, " failed: ", <string>returned.detail()?.message);
    }
}
