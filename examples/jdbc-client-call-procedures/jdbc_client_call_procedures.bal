import ballerina/io;
import ballerina/sql;
import ballerinax/jdbc;

// Client endpoint for MySQL database. This client endpoint can be used with any jdbc
// supported database by providing the corresponding jdbc url.
endpoint jdbc:Client testDB {
    url: "jdbc:mysql://localhost:3306/testdb",
    username: "test",
    password: "test",
    poolOptions: { maximumPoolSize: 5 },
    dbOptions: { useSSL: false }
};

// This is the type created to represent data row.
type Student record {
    int id;
    int age;
    string name;
};

public function main() {
    // Creates a table using the update operation. If the DDL
    // statement execution is successful, the `update` operation returns 0.
    io:println("The update operation - Creating table and procedures:");
    var ret = testDB->update("CREATE TABLE student(id INT AUTO_INCREMENT,
                         age INT, name VARCHAR(255), PRIMARY KEY (id))");
    handleUpdate(ret, "Create student table");

    // Create the stored procedure with IN parameters.
    ret = testDB->update("CREATE PROCEDURE INSERTDATA(IN pAge INT,
                       IN pName VARCHAR(255))
	                   BEGIN
                       INSERT INTO student(age, name) values (pAge, pName);
                       END");
    handleUpdate(ret, "Stored procedure with IN param creation");

    // Create the stored procedure with INOUT and OUT parameters.
    ret = testDB->update("CREATE PROCEDURE GETCOUNT (INOUT pID INT,
                          OUT pCount INT)
                          BEGIN
                          SELECT id INTO pID FROM student WHERE age = pID;
                          SELECT COUNT(*) INTO pCount FROM student
                            WHERE age = 20;
                          END");
    handleUpdate(ret, "Stored procedure with INOUT/OUT param creation");


    // Call operiation is used to invoke a stored procedure. Here stored procedure
    // with IN parameters is invoked.
    io:println("\nThe call operation - With IN params");
    // Invoke the stored procedure with IN type parameters.
    var retCall = testDB->call("{CALL INSERTDATA(?,?)}", (), 20, "George");
    match retCall {
        ()|table[] => io:println("Call operation with IN params successful");
        error e => io:println("Stored procedure call failed: "
                + e.message);
    }

    // Here stored procedure with OUT and INOUT parameters is invoked.
    io:println("\nThe call operation - With INOUT/OUT params");
    // Inovke the stored procedure.
    sql:Parameter param1 = { sqlType: sql:TYPE_INTEGER, value: 20,
        direction: sql:DIRECTION_INOUT };
    sql:Parameter param2 = { sqlType: sql:TYPE_INTEGER,
        direction: sql:DIRECTION_OUT };
    retCall = testDB->call("{CALL GETCOUNT(?,?)}", (), param1, param2);
    match retCall {
        ()|table[] => {
            io:println("Call operation with INOUT and OUT params successful");
            io:print("Student ID of the student with age = 20: ");
            io:println(param1.value);
            io:print("Student count with age = 20: ");
            io:println(param2.value);
        }
        error e => io:println("Stored procedure call failed: "
                + e.message);
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

// Function to handle return of the update operation.
function handleUpdate(int|error returned, string message) {
    match returned {
        int retInt => io:println(message + " status: " + retInt);
        error e => io:println(message + " failed: " + e.message);
    }
}

// Select data from the table and print.
function checkData() {
    var dtReturned = testDB->select("SELECT * FROM student", Student);

    table<Student> dt;
    match dtReturned {
        table tableReturned => dt = tableReturned;
        error e => io:println("Select data from student table failed: "
                + e.message);
    }

    // Iterating data.
    io:println("Data in students table:");
    foreach row in dt {
        io:println("Student:" + row.id + "|" + row.name + "|" + row.age);
    }
}
