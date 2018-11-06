import ballerina/io;
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

type myBatchType int|string;

public function main() {
    // Creates a table using the update operation. If the DDL
    // statement execution is successful, the `update` operation returns 0.
    io:println("The update operation - Creating a table:");
    var ret = testDB->update("CREATE TABLE student(id INT AUTO_INCREMENT,
                         age INT, name VARCHAR(255), PRIMARY KEY (id))");
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

    // Prepare the data batches.
    int datalen = lengthof jsonMsg.student;
    myBatchType[][] dataBatch;
    int i = 0;

    foreach (studentData in jsonMsg.student) {
        string name = studentData.firstname.toString();
        int age = check <int>studentData.age;

        myBatchType[] dataRow = [age, name];
        dataBatch[i] = dataRow;
        i = i + 1;
    }
    // A batch of data can be inserted using the `batchUpdate` operation. The number
    // of inserted rows for each insert in the batch is returned as an array.
    var retBatch = testDB->batchUpdate("INSERT INTO student
                    (age,name) VALUES (?,?)", ...dataBatch);
    match retBatch {
        int[] counts => {
            io:println("Batch 1 update counts: " + counts[0]);
            io:println("Batch 2 update counts: " + counts[1]);
        }
        error e => io:println("Batch update operation failed: " + e.message);
    }

    //Check the data in the database.
    checkData();

    io:println("\nThe update operation - Drop the student table");
    ret = testDB->update("DROP TABLE student");
    handleUpdate(ret, "Drop table student");
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
