import ballerina/sql;
import ballerina/mysql;
import ballerina/io;

endpoint mysql:Client testDB {
    host:"localhost",
    port:3306,
    name:"testdb",
    username:"root",
    password:"root",
    poolOptions:{maximumPoolSize:5}
};

function main(string... args) {

    //Create a DB table using the `update` action. If the DDL
    //statement execution is successful, the `update` action returns 0.
    var ret = testDB->update("CREATE TABLE STUDENT(ID INT AUTO_INCREMENT, AGE INT,
                                NAME VARCHAR(255), PRIMARY KEY (ID))");
    match ret {
        int status => io:println("Table creation status: " + status);
        error err => {
            io:println("STUDENT table creation failed: " + err.message);
            return;
        }
    }

    //Insert data using the `update` action. If the DML statement execution
    //is successful, the `update` action returns the updated row count.
    sql:Parameter para1 = (sql:TYPE_INTEGER, 8);
    sql:Parameter para2 = (sql:TYPE_VARCHAR, "Sam");
    ret = testDB->update("INSERT INTO STUDENT (AGE,NAME) VALUES (?,?)", para1, para2);
    match ret {
        int rows => io:println("Inserted row count: " + rows);
        error err => {
            io:println("Update action failed: " + err.message);
            return;
        }
    }

    //Select data using the `select` action. The `select` action returns a table.
    //See the `sql-queries-on-tables` ballerina example for more details on how to access data.
    var dtReturned = testDB->select("SELECT * FROM STUDENT WHERE AGE = ?", (), para1);
    table dt = check dtReturned;
    var j = check <json>dt;
    io:println(j);

    //Drop the STUDENT table.
    ret = testDB->update("DROP TABLE STUDENT");
    match ret {
        int status => io:println("Table drop status: " + status);
        error err => {
            io:println("Dropping STUDENT table failed: " + err.message);
            return;
        }
    }

    //Finally, close the connection pool.
    _ = testDB->close();
}
