import ballerina/io;
import ballerinax/java.jdbc;

// Client for MySQL database. This client can be used with any JDBC
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
    io:println("The update operation - Creating table and procedures:");
    var ret = testDB->update("CREATE TABLE student(id INT AUTO_INCREMENT, " +
                         "age INT, name VARCHAR(255), PRIMARY KEY (id))");
    handleUpdate(ret, "Create student table");

    // Create the stored procedure with IN parameters.
    ret = testDB->update("CREATE PROCEDURE INSERTDATA(IN pAge INT, " +
                       "IN pName VARCHAR(255)) " +
	                   "BEGIN " +
                       "INSERT INTO student(age, name) values (pAge, pName); " +
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


    // The remote function `call` is used to invoke a stored procedure.
    // Here the stored procedure with IN parameters is invoked.
    io:println("\nThe call operation - With IN params");
    // Invoke the stored procedure with IN type parameters.
    var retCall = testDB->call("{CALL INSERTDATA(?,?)}", (), 20, "George");
    if (retCall is ()|table<record {}>[]) {
        io:println("Call operation with IN params successful");
    } else {
        error err = retCall;
        io:println("Stored procedure call failed: ",
                 <string> err.detail()["message"]);
    }

    // Here stored procedure with OUT and INOUT parameters is invoked.
    io:println("\nThe call operation - With INOUT/OUT params");
    // Inovke the stored procedure.
    jdbc:Parameter param1 = { sqlType: jdbc:TYPE_INTEGER, value: 20,
        direction: jdbc:DIRECTION_INOUT };
    jdbc:Parameter param2 = { sqlType: jdbc:TYPE_INTEGER,
        direction: jdbc:DIRECTION_OUT };
    retCall = testDB->call("{CALL GETCOUNT(?,?)}", (), param1, param2);
    if (retCall is ()|table<record {}>[]) {
        io:println("Call operation with INOUT and OUT params successful");
        io:print("Student ID of the student with age = 20: ");
        io:println(param1.value);
        io:print("Student count with age = 20: ");
        io:println(param2.value);
    } else {
        error err = retCall;
        io:println("Stored procedure call failed: ",
                 <string> err.detail()["message"]);
    }

    checkData();

    //Drop the table and procedures.
    io:println("\nThe update operation - Drop the tables and procedures");
    ret = testDB->update("DROP TABLE student");
    handleUpdate(ret, "Drop table student");

    ret = testDB->update("DROP PROCEDURE INSERTDATA");
    handleUpdate(ret, "Drop stored procedure INSERTDATA");

    ret = testDB->update("DROP PROCEDURE GETCOUNT");
    handleUpdate(ret, "Drop stored procedure GETCOUNT");
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

// Select data from the table and print.
function checkData() {
    var dtReturned = testDB->select("SELECT * FROM student", Student);

    if (dtReturned is table<Student>) {
        // Iterating data.
        io:println("Data in students table:");
        foreach var row in dtReturned {
            io:println("Student:", row.id, "|", row.name, "|", row.age);
        }
    } else {
        error err = dtReturned;
        io:println("Select data from student table failed: ",
                 <string> err.detail()["message"]);
    }
}
