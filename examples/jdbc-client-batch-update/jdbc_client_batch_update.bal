import ballerina/io;
import ballerinax/java.jdbc;

// Client for the MySQL database. This client can be used with any JDBC
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

type myBatchType int|string;

public function main() {
    // Create a table using the `update` remote function.
    io:println("The update operation - Creating a table:");
    var ret = testDB->update("CREATE TABLE student(id INT AUTO_INCREMENT, " +
                             "age INT, name VARCHAR(255), PRIMARY KEY (id))");
    handleUpdate(ret, "Create student table");

    // This `json` input is used as the input data.
    json jsonMsg = [{
        "firstname": "Peter",
        "age": 10
    }, {
        "firstname": "John",
        "age": 15
    }, {
        "firstname": "James",
        "age": 12
    }];

    //Prepare the data batch by iterating the `json` array.
    myBatchType[][] dataBatch = [];
    if (jsonMsg is json[]) {
        foreach (var studentData in jsonMsg) {
            string name = <string>studentData.firstname;
            int age = <int>studentData.age;
            myBatchType[] dataRow = [age, name];
            dataBatch.push(dataRow);
        }
    }
    // A batch of data can be inserted using the `batchUpdate` remote function.
    jdbc:BatchUpdateResult retBatch = testDB->batchUpdate("INSERT INTO student " +
            "(age,name) VALUES (?,?)", false, ...dataBatch);
    error? e = retBatch.returnedError;
    if (e is error) {
        io:println("Batch update operation failed:", <string>e.detail()?.message);
    } else {
        // The number of inserted rows for each insert in the batch is returned as
        // an array.
        io:println("Batch 1 update counts: ", retBatch.updatedRowCount[0]);
        io:println("Batch 2 update counts: ", retBatch.updatedRowCount[1]);
        anydata[]? generatedKeys = retBatch.generatedKeys["GENERATED_KEY"];
        if (generatedKeys is int[]) {
            int key1 = generatedKeys[0];
            int key2 = generatedKeys[1];
            int key3 = generatedKeys[2];
            io:println("Generated keys are: ", key1, ", ", key2, " and ", key3);
        }
    }
    // Check the data in the database.
    checkData();
    // Delete the tables from the database.
    io:println("\nThe update operation - Drop the student table");
    ret = testDB->update("DROP TABLE student");
    handleUpdate(ret, "Drop table student");
}

// Function to handle the return value of the `update` remote function.
function handleUpdate(jdbc:UpdateResult|jdbc:Error returned, string message) {
    if (returned is jdbc:UpdateResult) {
        io:println(message, " status: ", returned.updatedRowCount);
    } else {
        io:println(message, " failed: ", <string>returned.detail()?.message);
    }
}

// Select data from the table and print.
function checkData() {
    var dtReturned = testDB->select("SELECT * FROM student", Student);
    if (dtReturned is table<Student>) {
        // Iterating data.
        io:println("Data in students table:");
        foreach var row in dtReturned {
            io:println(row.toString());
        }
    } else {
        io:println("Select data from student table failed: ",
                    <string>dtReturned.detail()?.message);
    }
}
