import ballerina/io;
import ballerina/h2;
import ballerina/sql;

// Create an endpoint for h2. Change the path before running the sample.
endpoint h2:Client testDB {
    path: "/home/ballerina/test/",
    name: "testdb",
    username: "SA",
    password: "",
    poolOptions: {maximumPoolSize: 5}
};

function main(string... args) {

    // Create a DB table using the `update` action. If the DDL
    // statement execution is successful, the `update` action returns 0.
    var ret = testDB->update("CREATE TABLE STUDENT(ID INTEGER, AGE INTEGER,
                                NAME VARCHAR(255), PRIMARY KEY (ID))");
    match ret {
        int status => io:println("Table creation status: " + status);
        error err => {
            handleError("STUDENT table creation failed: ", err, testDB);
            return;
        }
    }

    // Insert data using the `update` action. If the DML statement execution
    // is successful, the `update` action returns the updated row count.
    sql:Parameter p0 = {sqlType: sql:TYPE_INTEGER, value: 1};
    sql:Parameter p1 = {sqlType: sql:TYPE_INTEGER, value: 8};
    sql:Parameter p2 = {sqlType: sql:TYPE_VARCHAR, value: "Sam"};
    ret = testDB->update("INSERT INTO STUDENT (ID,AGE,NAME) VALUES (?,?,?)", p0, p1, p2);
    match ret {
        int rows => io:println("Inserted row count: " + rows);
        error err => {
            handleError("Update action failed: ", err, testDB);
            return;
        }
    }

    // Select data using the `select` action. The `select` action returns a table.
    // See the `table` ballerina example for more details on how to access data.
    var dtReturned = testDB->select("SELECT * FROM STUDENT WHERE AGE = ?", (), p1);
    table dt;
    match dtReturned {
        table val => dt = val;
        error e => {
            handleError("Select action failed: ", e, testDB);
            return;
        }
    }

    // Convert the returned table into json.
    var jsonConversionReturnVal = <json>dt;
    match jsonConversionReturnVal {
        json jsonRes => io:println(io:sprintf("%s", jsonRes));
        error e => io:println("Error in table to json conversion");
    }

    // Drop the STUDENT table.
    ret = testDB->update("DROP TABLE STUDENT");
    match ret {
        int status => io:println("Table drop status: " + status);
        error err => {
            handleError("Dropping STUDENT table failed: ", err, testDB);
            return;
        }
    }

    // Finally, close the connection pool.
    testDB.stop();
}

function handleError(string message, error e, h2:Client db) {
    io:println(message + e.message);
    db.stop();
}
