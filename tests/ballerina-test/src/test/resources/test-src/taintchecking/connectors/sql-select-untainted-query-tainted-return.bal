import ballerina.data.sql;

struct Employee {
    int id;
    string name;
}

public function main (string[] args) {
    testSelectWithUntaintedQueryProducingTaintedReturn(args);
}

public function testSelectWithUntaintedQueryProducingTaintedReturn(string[] args) {
    endpoint<sql:Client> testDBEP {
        database: sql:DB.MYSQL,
        host: "localhost",
        port: 3306,
        name: "testdb",
        username: "root",
        password: "root",
        options: {maximumPoolSize:5}
    }
    var testDB = testDBEP.getConnector();

    table dt = testDB -> select("SELECT  FirstName from Customers where registrationID = 1", null, null);
    while (dt.hasNext()) {
        var rs, _ = (Employee)dt.getNext();
        testFunction(rs.name);
    }
    testDB -> close();
    return;
}

public function testFunction (string anyValue) {

}
