import ballerina/io;
import ballerina/jdbc;
import ballerina/sql;

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
    io:println("The update operation - Creating a table:");
    var ret = testDB->update("CREATE TABLE student(id INT AUTO_INCREMENT,
                         age INT, name VARCHAR(255), PRIMARY KEY (id))");
    handleUpdate(ret, "Create student table");

    // Inserts data to the table using the update operation. If the DML statement execution
    // is successful, the `update` operation returns the updated row count.
    ret = testDB->update("INSERT INTO student(age, name) values (?, ?)",
        20, "John");
    handleUpdate(ret, "Insert to student table");
    ret = testDB->update("INSERT INTO student(age, name) values (?, ?)",
        10, "Peter");
    handleUpdate(ret, "Insert to student table");

    // A proxy for a database table that allows performing add/remove operations over
    // the actual database table, can be obtained by `getProxyTable` operation.
    io:println("\nThe getProxyTable operation - Get a proxy for a table");
    var proxyRet = testDB->getProxyTable("student", Student);
    table<Student> tbProxy;
    match proxyRet {
        table tbReturned => tbProxy = tbReturned;
        error e => io:println("Proxy table retrieval failed: " + e.message);
    }

    // Iterate through the table and retrieve the data record corresponding to each row.
    foreach row in tbProxy {
        io:println("Student:" + row.id + "|" + row.name + "|" + row.age);
    }

    // Data can be added to the database table through the proxied table.
    io:println("\nAdd data to a proxied table");
    Student s = { name: "Tim", age: 14 };
    var addRet = tbProxy.add(s);
    match addRet {
        () => io:println("Insertion to table successful");
        error e => io:println("Insertion to table failed: " + e.message);
    }

    // Data can be removed from the database table through the proxied table, by passing
    // a function pointer which returns a boolean value evaluating whether a given record
    // should be removed or not.
    io:println("\nRemove data from a proxied table");
    var rmRet = tbProxy.remove(isUnder20);
    match rmRet {
        int count => io:println("Removed count: " + count);
        error e => io:println("Removing from table failed: " + e.message);
    }

    //Drop the table and procedures.
    io:println("\nThe update operation - Drop the table");
    ret = testDB->update("DROP TABLE student");
    handleUpdate(ret, "Drop table student");

    // Finally, close the connection pool.
    testDB.stop();
}

// Check crieteria for remove.
function isUnder20(Student s) returns boolean {
    return s.age < 20;
}

// Function to handle return of the update operation.
function handleUpdate(int|error returned, string message) {
    match returned {
        int retInt => io:println(message + " status: " + retInt);
        error e => io:println(message + " failed: " + e.message);
    }
}
