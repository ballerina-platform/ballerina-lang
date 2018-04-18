import ballerina/sql;
import ballerina/mysql;
import ballerina/io;

endpoint mysql:Client testDB {
    host: "localhost",
    port: 3306,
    name: "testdb",
    username: "root",
    password: "root",
    poolOptions: {maximumPoolSize:5}
};

function main (string... args) {

    //Create a DB table using the `update` action. If the DDL
    //statement execution is successful, the `update` action returns 0.
    var ret = testDB -> update("CREATE TABLE STUDENT(ID INT AUTO_INCREMENT, AGE INT,
                                NAME VARCHAR(255), PRIMARY KEY (ID))", null);
    match ret {
        int status => {
            io:println("Table creation status:" + status);
        }
        error err => {
            io:println("SALARY table creation failed:" + err.message);
            return;
        }
    }

    //Create a stored procedure using the `update` action.
    ret = testDB -> update("CREATE PROCEDURE GETCOUNT (IN pAge INT, OUT pCount INT,
                         INOUT pInt INT)
                         BEGIN SELECT COUNT(*) INTO pCount FROM STUDENT
                              WHERE AGE = pAge; SELECT COUNT(*) INTO pInt FROM
                              STUDENT WHERE ID = pInt;
                         END", null);
    match ret {
        int status => {
            io:println("Stored proc creation status:" + status);
        }
        error err => {
            io:println("SALARY table creation failed:" + err.message);
            return;
        }
    }

    //Insert data using the `update` action. If the DML statement execution
    //is successful, the `update` action returns the updated row count.
    sql:Parameter[] params = [];
    sql:Parameter para1 = {sqlType:sql:TYPE_INTEGER, value:8};
    sql:Parameter para2 = {sqlType:sql:TYPE_VARCHAR, value:"Sam"};
    params = [para1, para2];
    ret = testDB -> update("INSERT INTO STUDENT (AGE,NAME) VALUES (?,?)", params);
    match ret {
        int rows => {
            io:println("Inserted row count:" + rows);
        }
        error err => {
            io:println("SALARY table Creation failed:" + err.message);
            return;
        }
    }

    //Column values generated during the update can be retrieved using the
    //`updateWithGeneratedKeys` action. If the table has several auto
    //generated columns other than the auto incremented key, those column
    //names should be given as an array. The values of the auto incremented
    //column and the auto generated columns are returned as a string array.
    //Similar to the `update` action, the inserted row count is also returned.
    var val = testDB -> updateWithGeneratedKeys("INSERT INTO STUDENT
                      (AGE,NAME) VALUES (?, ?)", params, null);
    match val {
        (int, string[]) y => {
            int count;
            string[] ids;
            (count, ids) = y;
            io:println("Inserted row count:" + count);
            io:println("Generated key:" + ids[0]);
        }
        error err1 => {
            io:println(err1.message);
        }
    }

    //Select data using the `select` action. The `select` action returns a table.
    //See the `sql-queries-on-tables` ballerina example for more details on how to access data.
    params = [para1];
    var dtReturned = testDB -> select("SELECT * FROM STUDENT WHERE AGE = ?", params, null);
    table dt = check dtReturned;
    string jsonRes;
    var j = check <json>dt;
    jsonRes = j.toString() but {() => ""};
    io:println(jsonRes);

    //A batch of data can be inserted using the `batchUpdate` action. The number
    //of inserted rows for each insert in the batch is returned as an array.
    sql:Parameter p1 = {sqlType:sql:TYPE_INTEGER, value:10};
    sql:Parameter p2 = {sqlType:sql:TYPE_VARCHAR, value:"Smith"};
    sql:Parameter[] item1 = [p1, p2];
    sql:Parameter p3 = {sqlType:sql:TYPE_INTEGER, value:20};
    sql:Parameter p4 = {sqlType:sql:TYPE_VARCHAR, value:"John"};
    sql:Parameter[] item2 = [p3, p4];
    sql:Parameter[][] bPara = [item1, item2];
    var insertVal = testDB -> batchUpdate("INSERT INTO STUDENT (AGE,NAME) VALUES (?, ?)", bPara);
    int[] default = [];
    int[] c = insertVal but {error => default};
    io:println("Batch item 1 status:" + c[0]);
    io:println("Batch item 2 status:" + c[1]);

    //A stored procedure can be invoked using the `call` action. The direction is
    //used to specify `IN`/`OUT`/`INOUT` parameters.
    sql:Parameter pAge = {sqlType:sql:TYPE_INTEGER, value:10};
    sql:Parameter pCount = {sqlType:sql:TYPE_INTEGER, direction:sql:DIRECTION_OUT};
    sql:Parameter pId = {sqlType:sql:TYPE_INTEGER, value:1, direction:sql:DIRECTION_INOUT};
    params = [pAge, pCount, pId];
    var results = testDB -> call("{CALL GETCOUNT(?,?,?)}", params, null);
    var countValue = <int>pCount.value;
    match countValue {
        int count => {
            io:println("Age 10 count:" + count);
        }
        error err1 => {
            io:println(err1.message);
        }
    }

    var idValue = <int>pId.value;
    match idValue {
        int id => {
            io:println("Id 1 count:" + id);
        }
        error err1 => {
            io:println(err1.message);
        }
    }
    //Drop the STUDENT table.
    ret = testDB -> update("DROP TABLE STUDENT", null);
    match ret {
        int status => {
            io:println("Table drop status:" + status);
        }
        error err => {
            io:println("SALARY table Creation failed:" + err.message);
            return;
        }
    }

    //Drop the GETCOUNT procedure.
    ret = testDB -> update("DROP PROCEDURE GETCOUNT", null);
    match ret {
        int status => {
            io:println("Procedure drop status:" + status);
        }
        error err => {
            io:println("SALARY table Creation failed:" + err.message);
            return;
        }
    }

    //Finally, close the connection pool.
    _ = testDB -> close();
}
