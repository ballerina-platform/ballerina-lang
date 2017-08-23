import ballerina.lang.system;
import ballerina.data.sql;

function main (string[] args) {
    //Create a SQL connector by providing the required database connection
    //pool properties.
    sql:ConnectionProperties properties = {maximumPoolSize:5};
    sql:ClientConnector testDB = create sql:ClientConnector(
      sql:MYSQL, "localhost", 3306, "testdb", "test", "test", properties);
    //Create a DB table using update action.If the DDL
    //statement execution is success update action returns 0
    sql:Parameter[] params = [];
    int ret = testDB.update("CREATE TABLE IF NOT EXISTS STUDENT(ID INT AUTO_INCREMENT,
                    AGE INT, NAME VARCHAR(255), PRIMARY KEY (ID))", params);
    system:println("Table creation status:" + ret);

    //Create a stored procedure using update action.
    ret = testDB.update("CREATE PROCEDURE GETCOUNT (IN pAge INT,
                  OUT pCount INT, INOUT pInt INT)
                  BEGIN SELECT COUNT(*) INTO pCount FROM STUDENT
                  WHERE AGE = pAge; SELECT COUNT(*) INTO pInt FROM
                  STUDENT WHERE ID = pInt; END", params);
    system:println("Stored proc creation status:" + ret);

    //Insert data using update action. If the DML statement execution
    //is success update action returns the updated row count.
    sql:Parameter para1 = {sqlType:"integer", value:8};
    sql:Parameter para2 = {sqlType:"varchar", value:"Sam"};
    params = [para1, para2];
    ret = testDB.update("INSERT INTO STUDENT (AGE,NAME) VALUES (?,?)",
                        params);
    system:println("Inserted row count:" + ret);

    //Column values generated during the update can be retrieved via
    //updateWithGeneratedKeys action. If the table has several auto
    //generated columns other than the auto incremented key, those column
    //names should be given as an array. The values of the auto incremented
    //column and the auto generated columns are returned as string array.
    //Similar to the update action inserted row count is also returned.
    string[] keyColumns = [];
    string[] ids;
    ret, ids = testDB.updateWithGeneratedKeys("INSERT INTO STUDENT
                      (AGE,NAME) VALUES (?, ?)", params, keyColumns);
    system:println("Inserted row count:" + ret);
    system:println("Generated key:" + ids[0]);
    
    //Select data using select action. Select action returns a datatable
    //and see datatables section for more details on how to access data.
    params = [para1];
    datatable dt = testDB.select("SELECT * FROM STUDENT WHERE AGE = ?",
                                 params);
    var jsonRes, err = <json>dt;
    system:println(jsonRes);

    //A Batch of data can be inserted using  batchUpdate action. Number
    //of inserted rows for each insert in batch is returned as an array
    sql:Parameter p1 = {sqlType:"integer", value:10};
    sql:Parameter p2 = {sqlType:"varchar", value:"Smith"};
    sql:Parameter[] item1 = [p1, p2];
    sql:Parameter p3 = {sqlType:"integer", value:20};
    sql:Parameter p4 = {sqlType:"varchar", value:"John"};
    sql:Parameter[] item2 = [p3, p4];
    sql:Parameter[][] bPara = [item1, item2];
    int[] count = testDB.batchUpdate("INSERT INTO STUDENT (AGE,NAME)
        VALUES (?, ?)", bPara);
    system:println("Batch item 1 status:" + count[0]);
    system:println("Batch item 2 status:" + count[1]);

    //A stored procedure can be invoked via call action. The direction is
    //used to specify in/out/input parameters. in - direction=0;
    //out - direction=1; inout - direction=2. Default directions is 0.
    sql:Parameter pAge = {sqlType:"integer", value:10};
    sql:Parameter pCount = {sqlType:"integer", direction:1};
    sql:Parameter pId = {sqlType:"integer", value:1, direction:2};
    params = [pAge, pCount, pId];
    testDB.call("{CALL GETCOUNT(?,?,?)}", params);
    var countValue, _ = (int)pCount.value;
    system:println("Age 10 count:" + countValue);
    var idValue, _ = (int)pId.value;
    system:println("Id 1 count:" + idValue);

    //Finally close the connection pool.
    testDB.close();
}
