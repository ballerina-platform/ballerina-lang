import ballerina.data.sql;
import ballerina.io;

function main (string[] args) {
    endpoint<sql:ClientConnector> testDB {
          create sql:ClientConnector(
          sql:DB.MYSQL, "localhost", 3306, "testdb", "root", "root", {maximumPoolSize:5});
    }
    //Create a DB table using updateQuery action.If the DDL
    //statement execution is success updateQuery action returns 0.
    int ret = testDB.updateQuery("CREATE TABLE STUDENT(ID INT AUTO_INCREMENT, AGE INT,
                                NAME VARCHAR(255), PRIMARY KEY (ID))", null);
    io:println("Table creation status:" + ret);

    //Create a stored procedure using updateQuery action.
    ret = testDB.updateQuery("CREATE PROCEDURE GETCOUNT (IN pAge INT, OUT pCount INT,
                         INOUT pInt INT)
                         BEGIN SELECT COUNT(*) INTO pCount FROM STUDENT
                              WHERE AGE = pAge; SELECT COUNT(*) INTO pInt FROM
                              STUDENT WHERE ID = pInt;
                         END", null);
    io:println("Stored proc creation status:" + ret);

    //Insert data using updateQuery action. If the DML statement execution
    //is success updateQuery action returns the updated row count.
    sql:Parameter[] params = [];
    sql:Parameter para1 = {sqlType:sql:Type.INTEGER, value:8};
    sql:Parameter para2 = {sqlType:sql:Type.VARCHAR, value:"Sam"};
    params = [para1, para2];
    ret = testDB.updateQuery("INSERT INTO STUDENT (AGE,NAME) VALUES (?,?)", params);
    io:println("Inserted row count:" + ret);

    //Column values generated during the updateQuery can be retrieved via
    //updateWithGeneratedKeys action. If the table has several auto
    //generated columns other than the auto incremented key, those column
    //names should be given as an array. The values of the auto incremented
    //column and the auto generated columns are returned as string array.
    //Similar to the updateQuery action, the inserted row count is also returned.
    var count, ids = testDB.updateWithGeneratedKeys("INSERT INTO STUDENT
                      (AGE,NAME) VALUES (?, ?)", params, null);
    io:println("Inserted row count:" + count);
    io:println("Generated key:" + ids[0]);

    //Select data using select action. Select action returns a datatable
    //and see datatables section for more details on how to access data.
    params = [para1];
    table dt = testDB.selectQuery("SELECT * FROM STUDENT WHERE AGE = ?", params, null);
    var jsonRes, err = <json>dt;
    io:println(jsonRes);

    //A Batch of data can be inserted using  batchUpdateQuery action. Number
    //of inserted rows for each insert in the batch is returned as an array.
    sql:Parameter p1 = {sqlType:sql:Type.INTEGER, value:10};
    sql:Parameter p2 = {sqlType:sql:Type.VARCHAR, value:"Smith"};
    sql:Parameter[] item1 = [p1, p2];
    sql:Parameter p3 = {sqlType:sql:Type.INTEGER, value:20};
    sql:Parameter p4 = {sqlType:sql:Type.VARCHAR, value:"John"};
    sql:Parameter[] item2 = [p3, p4];
    sql:Parameter[][] bPara = [item1, item2];
    int[] c = testDB.batchUpdateQuery("INSERT INTO STUDENT (AGE,NAME) VALUES (?, ?)", bPara);
    io:println("Batch item 1 status:" + c[0]);
    io:println("Batch item 2 status:" + c[1]);

    //A stored procedure can be invoked via call action. The direction is
    //used to specify in/out/inout parameters.
    sql:Parameter pAge = {sqlType:sql:Type.INTEGER, value:10};
    sql:Parameter pCount = {sqlType:sql:Type.INTEGER, direction:sql:Direction.IN};
    sql:Parameter pId = {sqlType:sql:Type.INTEGER, value:1, direction:sql:Direction.OUT};
    params = [pAge, pCount, pId];
    var results = testDB.call("{CALL GETCOUNT(?,?,?)}", params, null);
    var countValue, _ = (int)pCount.value;
    io:println("Age 10 count:" + countValue);
    var idValue, _ = (int)pId.value;
    io:println("Id 1 count:" + idValue);

    //Drop the STUDENT table.
    ret = testDB.updateQuery("DROP TABLE STUDENT", null);
    io:println("Table drop status:" + ret);

    //Drop the GETCOUNT procedure.
    ret = testDB.updateQuery("DROP PROCEDURE GETCOUNT", null);
    io:println("Procedure drop status:" + ret);

    //Finally close the connection pool.
    testDB.close();
}
