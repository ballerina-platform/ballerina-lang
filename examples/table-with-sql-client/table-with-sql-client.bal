import ballerina/log;
import ballerina/mysql;
import ballerina/sql;
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
        host:"localhost",
        port:3306,
        name:"testdb",
        username:"root",
        password:"123",
        poolOptions:{maximumPoolSize:5}
    };

    int count;
    table dt;
    int ret;

    //Create a table named EMPLOYEE and populate it with sample data.
    var createTableRetVal = testDB->update("CREATE TABLE EMPLOYEE (id INT,name
        VARCHAR(25),salary DOUBLE,status BOOLEAN,birthdate DATE,birthtime TIME,
        updated TIMESTAMP)");

    match createTableRetVal {
        int count => log:printInfo("Create table status: " + count);
        error e => {
            handleError("Error in executing CREATE TABLE EMPLOYEE", e, testDB);
            return;
        }
    }

    var insertTableRetVal = testDB->update("INSERT INTO EMPLOYEE VALUES(1, 'John', 1050.50, false,
        '1990-12-31', '11:30:45', '2007-05-23 09:15:28')");

    match insertTableRetVal {
        int count => log:printInfo("Updated row count: " + count);
        error e => {
           handleError("Error in executing INSERT INTO EMPLOYEE", e, testDB);
           return;
        }
    }

    var insertTableRetVal2 = testDB->update("INSERT INTO EMPLOYEE VALUES(2, 'Anne', 4060.50, true,
        '1999-12-31', '13:40:24', '2017-05-23 09:15:28')");

    match insertTableRetVal2 {
        int val => log:printInfo("Updated row count: " + val);
        error e => {
            handleError("Error in executing INSERT INTO EMPLOYEE", e, testDB);
            return;
        }
    }

    //Query the table using the SQL connector 'select' action. Either the 'select'
    //or 'call' action returns a table.
    var selectRetVal = testDB->select("SELECT * from EMPLOYEE", Employee);

    match selectRetVal {
        table val => dt = val;
        error e => {
            handleError("Error in executing SELECT * from EMPLOYEE", e, testDB);
            return;
        }
    }

    //Iterate through the result until hasNext() becomes false and retrieve
    //the data record corresponding to each row.
    while (dt.hasNext()) {
        var returnedNextRec = <Employee>dt.getNext();
        match returnedNextRec {
            Employee rs => {
                log:printInfo("Employee:" + rs.id + "|" + rs.name + "|" + rs.salary +
                        "|" + rs.status + "|" + rs.birthdate + "|"
                        + rs.birthtime + "|" + rs.updated);
            }
            error e => {
                handleError("Error in retrieving next record", e, testDB);
                return;
            }
        }
    }

    //Conversion from type 'table' to either JSON or XML results in data streaming. When a service client makes a request,
    //the result is streamed to the service client rather than building the full result in the server
    //and returning it. This allows unlimited payload sizes in the result and
    //the response is instantaneous to the client. <br>
    //Convert a table to JSON.
    var selectRetVal2 = testDB->select("SELECT id,name FROM EMPLOYEE", ());
    match selectRetVal2 {
        table val => dt = val;
        error e => {
            handleError("Error in executing SELECT id,name FROM EMPLOYEE", e, testDB);
            return;
        }
    }

    var jsonConversionReturnVal = <json>dt;

    match jsonConversionReturnVal {
        json jsonRes => log:printInfo(io:sprintf("%s", jsonRes));
        error e => log:printError("Error in table to json conversion");
    }

    //Convert a table to XML.
    var selectRetVal3 = testDB->select("SELECT id,name FROM EMPLOYEE", ());

    match selectRetVal3 {
        table val => dt = val;
        error e => {
            handleError("Error in executing SELECT id,name FROM EMPLOYEE", e, testDB);
            return;
        }
    }

    var xmlConversionReturnVal = <xml>dt;

    match xmlConversionReturnVal {
        xml xmlRes => log:printInfo(io:sprintf("%s", xmlRes));
        error e => log:printError("Error in table to xml conversion");
    }

    //Drop the EMPLOYEE table.
    var dropTableRetVal = testDB->update("DROP TABLE EMPLOYEE");
    match dropTableRetVal {
        int status => log:printInfo("Table drop status:" + status);
        error e => {
            handleError("Error in executing DROP TABLE EMPLOYEE", e, testDB);
            return;
        }
    }

    //Finally close the DB connection.
    testDB.stop();
}

function handleError(string message, error e, mysql:Client db) {
    endpoint mysql:Client testDB = db;
    log:printError(message, err = e);
    testDB.stop();
}
