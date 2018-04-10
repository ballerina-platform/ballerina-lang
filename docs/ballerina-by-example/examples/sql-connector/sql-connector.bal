import ballerina/sql;
import ballerina/io;

endpoint sql:Client testDB {
    database:sql:DB_MYSQL,
    host:"localhost",
    port:3306,
    name:"testdb",
    username:"root",
    password:"root",
    options:{maximumPoolSize:5}
};

function main (string[] args) {

    //Create a DB table using update action.If the DDL
    //statement execution is success update action returns 0.
    var ret = testDB -> update("CREATE TABLE STUDENT(ID INT AUTO_INCREMENT, AGE INT,
                                NAME VARCHAR(255), PRIMARY KEY (ID))", null);
    match ret {
        int status => {
            io:println("Table creation status:" + status);
        }
        sql:SQLConnectorError err => {
            io:println("SALARY table Creation failed:" + err.message);
            return;
        }
    }

    //Create a stored procedure using update action.
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
        sql:SQLConnectorError err => {
            io:println("SALARY table Creation failed:" + err.message);
            return;
        }
    }

    //Insert data using update action. If the DML statement execution
    //is success update action returns the updated row count.
    sql:Parameter[] params = [];
    sql:Parameter para1 = {sqlType:sql:TYPE_INTEGER, value:8};
    sql:Parameter para2 = {sqlType:sql:TYPE_VARCHAR, value:"Sam"};
    params = [para1, para2];
    ret = testDB -> update("INSERT INTO STUDENT (AGE,NAME) VALUES (?,?)", params);
    match ret {
        int rows => {
            io:println("Inserted row count:" + rows);
        }
        sql:SQLConnectorError err => {
            io:println("SALARY table Creation failed:" + err.message);
            return;
        }
    }

    //Column values generated during the update can be retrieved via
    //updateWithGeneratedKeys action. If the table has several auto
    //generated columns other than the auto incremented key, those column
    //names should be given as an array. The values of the auto incremented
    //column and the auto generated columns are returned as string array.
    //Similar to the update action, the inserted row count is also returned.
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
        sql:SQLConnectorError err1 => {
            io:println(err1.message);
        }
    }

    //Select data using select action. Select action returns a table
    //and see tables section for more details on how to access data.
    params = [para1];
    var dtReturned = testDB -> select("SELECT * FROM STUDENT WHERE AGE = ?", params, null);
    table dt = check dtReturned;
    string jsonRes;
    var j = check <json>dt;
    jsonRes = j.toString() but {() => ""};
    io:println(jsonRes);

    //A Batch of data can be inserted using  batchUpdate action. Number
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
    int[] c = insertVal but {sql:SQLConnectorError => default};
    io:println("Batch item 1 status:" + c[0]);
    io:println("Batch item 2 status:" + c[1]);

    //A stored procedure can be invoked via call action. The direction is
    //used to specify in/out/inout parameters.
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
        sql:SQLConnectorError err => {
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
        sql:SQLConnectorError err => {
            io:println("SALARY table Creation failed:" + err.message);
            return;
        }
    }

    //Finally close the connection pool.
    _ = testDB -> close();
}
