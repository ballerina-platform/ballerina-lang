import ballerina/lang.system;
import ballerina/data.sql;

function main (string... args) {
    //Create a SQL connector by providing the required database connection
    //pool properties.
    map props = {"jdbcUrl":"jdbc:mysql://localhost:3306/db",
                    "username":"root", "password":"root"};
    sql:ClientConnector testDB = create
                                 sql:ClientConnector(props);
    //Create a DB table using update action.If the DDL
    //statement execution is success update action returns 0
    sql:Parameter[] params = [];
    int ret = sql:ClientConnector.update(testDB,
                    "create table Students(id int auto_increment,  " +
                    "age int, name varchar(255), primary key (id))", params);
    system:println("Table creation status:" + ret);

    //Create a stored procedure using update action.
    ret = sql:ClientConnector.update(testDB,
                 "create procedure getCount (IN pAge int,
                      OUT pCount int, INOUT pInt int)
                  begin select count(*) into pCount from Students
                  where age = pAge; select count(*) into pInt from
                  Students where id = pInt; end", params);
    system:println("Stored proc creation status:" + ret);

    //Insert data using update action. If the DML statement execution
    //is success update action returns the updated row count.
    sql:Parameter para1 = {sqlType:"integer", value:8};
    sql:Parameter para2 = {sqlType:"varchar", value:"Sam"};
    params = [para1, para2];
    ret = sql:ClientConnector.update(testDB,
                      "Insert into Students (age,name) values (?,?)",
                       params);
    system:println("Inserted row count:" + ret);

    //Column values generated during the update can be retrieved via
    //updateWithGeneratedKeys action. If the table has several auto
    //generated columns other than the auto incremented key, those column
    //names should be given as an array. The values of the auto incremented
    //column and the auto generated columns are returned as string array.
    //Similar to the update action inserted row count is also returned.
    string... keyColumns = [];
    string... ids;
    ret, ids = sql:ClientConnector.updateWithGeneratedKeys
               (testDB, "Insert into Students (age,name) values (?,?)",
                params, keyColumns);
    system:println("Inserted row count:" + ret);
    system:println("Generated key:" + ids[0]);
    
    //Select data using select action. Select action returns a datatable
    //and see datatables section for more details on how to access data.
    params = [para1];
    datatable dt = sql:ClientConnector.select(testDB,
                                "SELECT * from Students where age = ?", params);
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

    int[] count = sql:ClientConnector.batchUpdate(testDB,
                         "Insert into Students (age,name) values (?,?)", bPara);

    system:println("Batch item 1 status:" + count[0]);
    system:println("Batch item 2 status:" + count[1]);

    //A stored procedure can be invoked via call action. The direction is
    //used to specify in/out/input parameters. in - direction=0;
    //out - direction=1; inout - direction=2. Default directions is 0.
    sql:Parameter pAge = {sqlType:"integer", value:10};
    sql:Parameter pCount = {sqlType:"integer", direction:1};
    sql:Parameter pId = {sqlType:"integer", value:1, direction:2};
    params = [pAge, pCount, pId];
    
    sql:ClientConnector.call(testDB, "{call getCount(?,?,?)}", params);

    var countValue, _ = (int)pCount.value;
    system:println("Age 10 count:" + countValue);
    
    var idValue, _ = (int)pId.value;
    system:println("Id 1 count:" + idValue);

    //Finally close the connection pool.
    sql:ClientConnector.close(testDB);
}
