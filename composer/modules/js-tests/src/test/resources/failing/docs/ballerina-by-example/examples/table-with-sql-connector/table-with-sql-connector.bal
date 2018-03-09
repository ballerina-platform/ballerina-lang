import ballerina.data.sql;
import ballerina.io;

@Description {value:"This is the Employee struct. The field names of this should match column names of the table. The field types should match with the sql types."}
struct Employee {
    int id;
    string name;
    float salary;
    boolean status;
    string birthdate;
    string birthtime;
    string updated;
}

function main (string[] args) {
    endpoint<sql:ClientConnector> testDB {
      create sql:ClientConnector(sql:DB.MYSQL, "localhost", 3306,
                          "testdb", "root", "root", {maximumPoolSize:5});
    }

    //Create table named EMPLOYEE and populate sample data.
    int count = testDB.update("CREATE TABLE EMPLOYEE (id INT,name
        VARCHAR(25),salary DOUBLE,status BOOLEAN,birthdate DATE,birthtime TIME,
        updated TIMESTAMP)", null);
    count = testDB.update("INSERT INTO EMPLOYEE VALUES(1, 'John', 1050.50, false,
        '1990-12-31', '11:30:45', '2007-05-23 09:15:28')", null);
    count = testDB.update("INSERT INTO EMPLOYEE VALUES(2, 'Anne', 4060.50, true,
        '1999-12-31', '13:40:24', '2017-05-23 09:15:28')", null);

    //Query the table using SQL connector select action. Either select or call
    //action can return a table.
    table dt = testDB.select("SELECT * from EMPLOYEE", null, typeof Employee);
    //Iterate through the result until hasNext() become false and retrieve
    //the data struct corresponding to each row.
    while (dt.hasNext()) {
        var rs, _ = (Employee)dt.getNext();
        io:println("Employee:"+ rs.id + "|" + rs.name +  "|" + rs.salary +
              "|" + rs.status + "|" + rs.birthdate + "|"
              + rs.birthtime + "|" + rs.updated);
    }

    //The table to json/xml conversion is resulted in streamed data. With the data
    //streaming functionality, when a service client makes a request, the result is
    //streamed to the service client rather than building the full result in the server
    //and returning it. This allows virtually unlimited payload sizes in the result, and
    //the response is instantaneous to the client. <br>
    //Convert a table to JSON.
    dt = testDB.select("SELECT id,name FROM EMPLOYEE", null, null);
    var jsonRes, _ = <json>dt;
    io:println(jsonRes);

    //Convert a table to XML.
    dt = testDB.select("SELECT id,name FROM EMPLOYEE", null, null);
    var xmlRes, _ = <xml>dt;
    io:println(xmlRes);

    //Drop the EMPLOYEE table.
    int ret = testDB.update("DROP TABLE EMPLOYEE", null);
    io:println("Table drop status:" + ret);

    //Finally close the DB connection.
    testDB.close();
}
