import ballerina/io;
import ballerinax/java.jdbc;

// Client for the MySQL database. This client can be used with any JDBC
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

type myBatchType int|string?;

public function main() {
    // Create a table using the `update` remote function. If the DDL
    // statement execution is successful, the `update` remote function
    // returns 0.
    io:println("The update operation - Creating a table:");
    var ret = testDB->update("CREATE TABLE student(id INT AUTO_INCREMENT, " +
                         "age INT, name VARCHAR(255), PRIMARY KEY (id))");
    handleUpdate(ret, "Create student table");

    json jsonMsg = {
        "student": [{
            "firstname": "Peter",
            "age": 10
        }, {
            "firstname": "John",
            "age": 15
        }, {
            "firstname": "James",
            "age": 12
        }]
    };

    int datalen = 0;
    // Prepare the data batches.
    json|error students = jsonMsg.student;
    if (students is json[]) {
        datalen = students.length();
    }

    myBatchType[][] dataBatch = [];
    int i = 0;

    if (students is json[]) {
        foreach (var studentData in students) {
            string name = studentData.firstname.toString();
            int age = <int>studentData.age;

            myBatchType?[] dataRow = [age, name];
            dataBatch[i] = dataRow;
            i = i + 1;
        }
    }
    // A batch of data can be inserted using the `batchUpdate` remote function.
    // The number of inserted rows for each insert in the batch is returned as
    // an array.
    jdbc:BatchUpdateResult retBatch = testDB->batchUpdate("INSERT INTO student " +
                    "(age,name) VALUES (?,?)", false, ...dataBatch);
    error? e = retBatch.returnedError;
    if (e is error) {
        io:println("Batch update operation failed:",
                    <string> e.detail()["message"]);
    } else {
        io:println("Batch 1 update counts: ", retBatch.updatedRowCount[0]);
        io:println("Batch 2 update counts: ", retBatch.updatedRowCount[1]);
        anydata[]? generatedKeys = retBatch.generatedKeys["GENERATED_KEY"];
        if (generatedKeys is int[]) {
            int key1 = generatedKeys[0];
            int key2 = generatedKeys[1];
            int key3 = generatedKeys[2];
            io:println("Generated keys are: " , key1, ", ", key2, " and ", key3);
        }
    }

    // Check the data in the database.
    checkData();

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
