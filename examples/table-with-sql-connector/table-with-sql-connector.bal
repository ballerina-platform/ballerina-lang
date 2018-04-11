import ballerina/sql;
import ballerina/mysql;
import ballerina/io;

@Description {value:"This is the Employee struct. The field names of this should match column names of the table. The field types should match with the sql types."}
type Employee {
    int id;
    string name;
    float salary;
    boolean status;
    string birthdate;
    string birthtime;
    string updated;
};

function main (string[] args) {

    endpoint mysql:Client testDB {
        host: "localhost",
        port: 3306,
        name: "testdb",
        username: "root",
        password: "root",
        poolOptions: {maximumPoolSize:5}
    };

    int count;
    table dt;
    int ret;

    //Create table named EMPLOYEE and populate sample data.
    var returnValue = testDB -> update("CREATE TABLE EMPLOYEE (id INT,name
        VARCHAR(25),salary DOUBLE,status BOOLEAN,birthdate DATE,birthtime TIME,
        updated TIMESTAMP)", null);

    match returnValue {
        int val => {
            count = val;
        }
        error e => io:println("Error in executing CREATE TABLE EMPLOYEE");
    }

    returnValue = testDB -> update("INSERT INTO EMPLOYEE VALUES(1, 'John', 1050.50, false,
        '1990-12-31', '11:30:45', '2007-05-23 09:15:28')", null);

    match returnValue {
        int val => {
            count = val;
        }
        error e => io:println("Error in executing INSERT INTO EMPLOYEE");
    }

    returnValue = testDB -> update("INSERT INTO EMPLOYEE VALUES(2, 'Anne', 4060.50, true,
        '1999-12-31', '13:40:24', '2017-05-23 09:15:28')", null);

    match returnValue {
        int val => {
          count = val;
        }
        error e => io:println("Error in executing INSERT INTO EMPLOYEE");
    }

    //Query the table using SQL connector select action. Either select or call
    //action can return a table.
    var returnVal = testDB -> select("SELECT * from EMPLOYEE", null, Employee);

    match returnVal {
        table val => {
            dt = val;
        }
        error e => io:println("Error in executing SELECT * from EMPLOYEE");
    }

    //Iterate through the result until hasNext() become false and retrieve
    //the data struct corresponding to each row.
    while (dt.hasNext()) {
        var returnedNextRec = <Employee>dt.getNext();
        match returnedNextRec {
            Employee rs => {
                io:println("Employee:"+ rs.id + "|" + rs.name +  "|" + rs.salary +
                "|" + rs.status + "|" + rs.birthdate + "|"
                 + rs.birthtime + "|" + rs.updated);
            }
            error e => io:println("Error in retrieving next record");
        }
    }

    //The table to json/xml conversion is resulted in streamed data. With the data
    //streaming functionality, when a service client makes a request, the result is
    //streamed to the service client rather than building the full result in the server
    //and returning it. This allows virtually unlimited payload sizes in the result, and
    //the response is instantaneous to the client. <br>
    //Convert a table to JSON.
    var returnVal2 = testDB -> select("SELECT id,name FROM EMPLOYEE", null, null);
    match returnVal2 {
        table val => {
            dt = val;
        }
        error e => io:println("Error in executing SELECT id,name FROM EMPLOYEE");
    }

    var jsonRes = <json>dt;
    io:println(jsonRes);

    //Convert a table to XML.
    var returnVal3 = testDB -> select("SELECT id,name FROM EMPLOYEE", null, null);

    match returnVal3 {
        table val => {
            dt = val;
        }
        error e => io:println("Error in executing SELECT id,name FROM EMPLOYEE");
    }

    var xmlRes = <xml>dt;
    io:println(xmlRes);

    //Drop the EMPLOYEE table.
    var returnVal4 = testDB -> update("DROP TABLE EMPLOYEE", null);
    match returnVal4 {
        int val => {
            ret = val;
        }
        error e => io:println("Error in executing DROP TABLE EMPLOYEE");
    }
    io:println("Table drop status:" + ret);

    //Finally close the DB connection.
    var onConnectionClose = testDB -> close();
    match onConnectionClose {
        error e => io:println("Error in DB Connection close");
        any | () => {
            io:println("DB Connection closed successfully.");
        }
    }
}
