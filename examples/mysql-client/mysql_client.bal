import ballerina/io;
import ballerina/log;
import ballerina/mysql;
import ballerina/sql;

endpoint mysql:Client testDB {
    host: "localhost",
    port: 3306,
    name: "testdb",
    username: "root",
    password: "root",
    poolOptions: {maximumPoolSize: 5}
};

function main(string... args) {

    // Create a DB table using the `update` action. If the DDL
    // statement execution is successful, the `update` action returns 0.
    var ret = testDB->update("CREATE TABLE STUDENT(ID INT AUTO_INCREMENT, AGE INT,
                                NAME VARCHAR(255), PRIMARY KEY (ID))");
    match ret {
        int status => log:printInfo("Table creation status: " + status);
        error err => {
            handleError("STUDENT table creation failed", err, testDB);
            return;
        }
    }

    // Insert data using the `update` action. If the DML statement execution
    // is successful, the `update` action returns the updated row count.
    sql:Parameter para1 = {sqlType: sql:TYPE_INTEGER, value: 8};
    sql:Parameter para2 = {sqlType: sql:TYPE_VARCHAR, value: "Sam"};
    ret = testDB->update("INSERT INTO STUDENT (AGE,NAME) VALUES (?,?)", para1, para2);
    match ret {
        int rows => log:printInfo("Inserted row count: " + rows);
        error err => {
            handleError("Update action failed", err, testDB);
            return;
        }
    }

    // Select data using the `select` action. The `select` action returns a table.
    // See the `sql-queries-on-tables` ballerina example for more details on how to access data.
    var dtReturned = testDB->select("SELECT * FROM STUDENT WHERE AGE = ?", (), para1);
    table dt;
    match dtReturned {
        table val => dt = val;
        error e => {
            handleError("Select action failed", e, testDB);
            return;
        }
    }

    var jsonConversionReturnVal = <json>dt;

    match jsonConversionReturnVal {
        json jsonRes => log:printInfo(io:sprintf("%s", jsonRes));
        error e => log:printError("Error in table to json conversion");
    }

    // Drop the STUDENT table.
    ret = testDB->update("DROP TABLE STUDENT");
    match ret {
        int status => log:printInfo("Table drop status: " + status);
        error err => {
            handleError("Dropping STUDENT table failed", err, testDB);
            return;
        }
    }

    // Finally, close the connection pool.
    testDB.stop();
}

function handleError(string message, error e, mysql:Client db) {
    endpoint mysql:Client testDB = db;
    log:printError(message, err = e);
    testDB.stop();
}
