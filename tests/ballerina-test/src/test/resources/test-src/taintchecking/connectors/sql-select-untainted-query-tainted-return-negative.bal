import ballerina.data.sql;

struct Employee {
    int id;
    string name;
}

public function main (string[] args) {
    testSelectWithUntaintedQueryProducingTaintedReturnNegative(args);
}

public function testSelectWithUntaintedQueryProducingTaintedReturnNegative(string[] args) {
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
        testFunction(rs.name, rs.name);
    }
    testDB -> close();
    return;
}

public function testFunction (@sensitive string sensitiveValue, string anyValue) {

}
