import ballerina.data.sql;

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
      create sql:ClientConnector(
             sql:MYSQL, "localhost", 3306, "testdb", "root", "root", {maximumPoolSize:5});
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
    //action can return a datatable.
    datatable dt = testDB.select("SELECT * from EMPLOYEE", null);
    //Iterate through the result until hasNext() become false and retrieve
    //the data struct corresponding to each row.
    while (dt.hasNext()) {
        var rs, _ = (Employee)dt.getNext();
        println("Employee:"+ rs.id + "|" + rs.name +  "|" + rs.salary + "|" + rs.status +
                "|" + rs.birthdate + "|" + rs.birthtime + "|" + rs.updated);
    }

    //Convert a datatable to JSON.
    dt = testDB.select("SELECT id,name FROM EMPLOYEE", null);
    var jsonRes, _ = <json>dt;
    println(jsonRes);

    //Convert a datatable to XML.
    dt = testDB.select("SELECT id,name FROM EMPLOYEE", null);
    var xmlRes, _ = <xml>dt;
    println(xmlRes);

    //Drop the EMPLOYEE table.
    int ret = testDB.update("DROP TABLE EMPLOYEE", null);
    println("Table drop status:" + ret);

    //Finally close the DB connection.
    testDB.close();
}
