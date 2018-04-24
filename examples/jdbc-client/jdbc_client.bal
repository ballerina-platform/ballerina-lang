import ballerina/io;
import ballerina/jdbc;
import ballerina/sql;

endpoint jdbc:Client testDB {
    url: "jdbc:mysql://localhost:3306/testdb",
    username: "root",
    password: "root",
    poolOptions: { maximumPoolSize: 5 },
    dbOptions: { useSSL: false }
};

function main(string... args) {

    // Create a DB table using the `update` action. If the DDL
    // statement execution is successful, the `update` action returns 0.
    var ret = testDB->update("CREATE TABLE STUDENT(ID INT AUTO_INCREMENT, AGE INT,
                                NAME VARCHAR(255), PRIMARY KEY (ID))");
    match ret {
        int status => io:println("Table creation status: " + status);
        error err => {
            handleError("STUDENT table creation failed", err, testDB);
            return;
        }
    }

    // Create a stored procedure using the `update` action.
    ret = testDB->update("CREATE PROCEDURE GETCOUNT (IN pAge INT, OUT pCount INT,
                         INOUT pInt INT)
                         BEGIN SELECT COUNT(*) INTO pCount FROM STUDENT
                              WHERE AGE = pAge; SELECT COUNT(*) INTO pInt FROM
                              STUDENT WHERE ID = pInt;
                         END");
    match ret {
        int status => io:println("Stored proc creation status: " + status);
        error err => {
            handleError("GETCOUNT procedure creation failed", err, testDB);
            return;
        }
    }

    // Insert data using the `update` action. If the DML statement execution
    // is successful, the `update` action returns the updated row count.
    sql:Parameter para1 = {sqlType: sql:TYPE_INTEGER, value: 8};
    sql:Parameter para2 = {sqlType: sql:TYPE_VARCHAR, value: "Sam"};
    ret = testDB->update("INSERT INTO STUDENT (AGE,NAME) VALUES (?,?)", para1, para2);
    match ret {
        int rows => io:println("Inserted row count: " + rows);
        error err => {
            handleError("Update action failed", err, testDB);
            return;
        }
    }

    // Column values generated during the update can be retrieved using the
    // `updateWithGeneratedKeys` action. If the table has several auto
    // generated columns other than the auto incremented key, those column
    // names should be given as an array. The values of the auto incremented
    // column and the auto generated columns are returned as a string array.
    // Similar to the `update` action, the inserted row count is also returned.
    var val = testDB->updateWithGeneratedKeys("INSERT INTO STUDENT
                      (AGE,NAME) VALUES (?, ?)", (), para1, para2);
    match val {
        (int, string[]) y => {
            int count;
            string[] ids;
            (count, ids) = y;
            io:println("Inserted row count: " + count);
            io:println("Generated key: " + ids[0]);
        }
        error err => {
            handleError("Update action failed", err, testDB);
            return;
        }
    }

    // Select data using the `select` action. The `select` action returns a table.
    // See the `table_with_jdbc_client` ballerina example for more details on how to access data.
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
        json jsonRes => io:println(io:sprintf("%s", jsonRes));
        error e => io:println("Error in table to json conversion");
    }

    // A batch of data can be inserted using the `batchUpdate` action. The number
    // of inserted rows for each insert in the batch is returned as an array.
    sql:Parameter p1 = {sqlType: sql:TYPE_INTEGER, value: 10};
    sql:Parameter p2 = {sqlType: sql:TYPE_VARCHAR, value: "Smith"};
    sql:Parameter[] item1 = [p1, p2];
    sql:Parameter p3 = {sqlType: sql:TYPE_INTEGER, value: 20};
    sql:Parameter p4 = {sqlType: sql:TYPE_VARCHAR, value: "John"};
    sql:Parameter[] item2 = [p3, p4];
    var insertVal = testDB->batchUpdate("INSERT INTO STUDENT (AGE,NAME) VALUES (?, ?)", item1, item2);
    int[] default = [];
    int[] c = insertVal but { error => default };
    io:println("Batch item 1 status: " + c[0]);
    io:println("Batch item 2 status: " + c[1]);

    // A stored procedure can be invoked using the `call` action. The direction is
    // used to specify `IN`/`OUT`/`INOUT` parameters.
    sql:Parameter pAge = {sqlType: sql:TYPE_INTEGER, value: 10};
    sql:Parameter pCount = {sqlType: sql:TYPE_INTEGER, value: (), direction: sql:DIRECTION_OUT};
    sql:Parameter pId = {sqlType: sql:TYPE_INTEGER, value: 1, direction: sql:DIRECTION_INOUT};

    var results = testDB->call("{CALL GETCOUNT(?,?,?)}", (), pAge, pCount, pId);

    // Obtain the values of OUT/INOUT parameters
    int countValue = <int>pCount.value but { error => -1 };
    io:println("Age 10 count: " + countValue);

    int idValue = <int>pId.value but { error => -1 };
    io:println("Id 1 count: " + idValue);

    // Drop the STUDENT table.
    ret = testDB->update("DROP TABLE STUDENT");
    match ret {
        int status => io:println("Table drop status: " + status);
        error err => {
            handleError("Dropping STUDENT table failed", err, testDB);
            return;
        }
    }

    // Drop the GETCOUNT procedure.
    ret = testDB->update("DROP PROCEDURE GETCOUNT");
    match ret {
        int status => io:println("Procedure drop status: " + status);
        error err => {
            handleError("Dropping GETCOUNT procedure failed", err, testDB);
            return;
        }
    }

    // Finally, close the connection pool.
    testDB.stop();
}

function handleError(string message, error e, jdbc:Client db) {
    io:println(message + ": " + e.message);
    db.stop();
}
