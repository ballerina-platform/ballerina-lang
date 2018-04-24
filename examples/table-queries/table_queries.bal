import ballerina/sql;
import ballerina/mysql;
import ballerina/io;

@Description {value:
"This is a type called 'Employee'. The field names of this type should match the column names of the table and
the field types should match the sql types."
}
type Employee {
    int id,
    string name,
    float salary,
    boolean status,
    string birthdate,
    string birthtime,
    string updated,
};

function main(string... args) {

    endpoint mysql:Client testDB {
        host: "localhost",
        port: 3306,
        name: "testdb",
        username: "root",
        password: "root",
        poolOptions: {maximumPoolSize: 5},
        dbOptions: {useSSL: false}
    };

    int count;
    table dt;
    int ret;

    // Create a table named EMPLOYEE and populate it with sample data.
    var returnValue = testDB->update("CREATE TABLE EMPLOYEE (id INT,name
        VARCHAR(25),salary DOUBLE,status BOOLEAN,birthdate DATE,birthtime TIME,
        updated TIMESTAMP)");

    match returnValue {
        int val => count = val;
        error e => io:println("Error in executing CREATE TABLE EMPLOYEE");
    }

    returnValue = testDB->update("INSERT INTO EMPLOYEE VALUES(1, 'John', 1050.50, false,
        '1990-12-31', '11:30:45', '2007-05-23 09:15:28')");

    match returnValue {
        int val => count = val;
        error e => io:println("Error in executing INSERT INTO EMPLOYEE");
    }

    returnValue = testDB->update("INSERT INTO EMPLOYEE VALUES(2, 'Anne', 4060.50, true,
        '1999-12-31', '13:40:24', '2017-05-23 09:15:28')");

    match returnValue {
        int val => count = val;
        error e => io:println("Error in executing INSERT INTO EMPLOYEE");
    }

    // Query the table using the SQL connector 'select' action. Either the 'select'
    // or 'call' action returns a table.
    var returnVal = testDB->select("SELECT * from EMPLOYEE", Employee);

    match returnVal {
        table val => dt = val;
        error e => io:println("Error in executing SELECT * from EMPLOYEE");
    }

    // Iterate through the result until hasNext() becomes false and retrieve
    // the data record corresponding to each row.
    while (dt.hasNext()) {
        var returnedNextRec = <Employee>dt.getNext();
        match returnedNextRec {
            Employee rs => {
                io:println("Employee:" + rs.id + "|" + rs.name + "|" + rs.salary +
                        "|" + rs.status + "|" + rs.birthdate + "|"
                        + rs.birthtime + "|" + rs.updated);
            }
            error e => io:println("Error in retrieving next record");
        }
    }

    // Conversion from type 'table' to either JSON or XML results in data streaming. When a service client makes a request,
    // the result is streamed to the service client rather than building the full result in the server
    // and returning it. This allows unlimited payload sizes in the result and
    // the response is instantaneous to the client. <br>
    // Convert a table to JSON.
    var returnVal2 = testDB->select("SELECT id,name FROM EMPLOYEE", ());
    match returnVal2 {
        table val => dt = val;
        error e => io:println("Error in executing SELECT id,name FROM EMPLOYEE");
    }

    json jsonRes = check <json>dt;
    io:println(jsonRes);

    // Convert a table to XML.
    var returnVal3 = testDB->select("SELECT id,name FROM EMPLOYEE", ());

    match returnVal3 {
        table val => dt = val;
        error e => io:println("Error in executing SELECT id,name FROM EMPLOYEE");
    }

    xml xmlRes = check <xml>dt;
    io:println(xmlRes);

    // Drop the EMPLOYEE table.
    var returnVal4 = testDB->update("DROP TABLE EMPLOYEE");
    match returnVal4 {
        int val => ret = val;
        error e => io:println("Error in executing DROP TABLE EMPLOYEE");
    }
    io:println("Table drop status:" + ret);

    // Finally close the DB connection.
    testDB.stop();
}
