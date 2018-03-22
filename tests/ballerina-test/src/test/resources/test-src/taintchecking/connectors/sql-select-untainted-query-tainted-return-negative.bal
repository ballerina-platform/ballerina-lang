import ballerina/data.sql;

struct Employee {
    int id;
    string name;
}

public function main (string[] args) {
    testSelectWithUntaintedQueryProducingTaintedReturnNegative(args);
}

public function testSelectWithUntaintedQueryProducingTaintedReturnNegative(string[] args) {
    endpoint sql:Client testDB {
        database: sql:DB.MYSQL,
        host: "localhost",
        port: 3306,
        name: "testdb",
        username: "root",
        password: "root",
        options: {maximumPoolSize:5}
    };

    table dt = testDB -> select("SELECT  FirstName from Customers where registrationID = 1", null, null);
    while (dt.hasNext()) {
        var rs = <Employee>dt.getNext();
        match rs {
            Employee emp => testFunction(emp.name, emp.name);
            error => return;
        }
    }
    testDB -> close();
    return;
}

public function testFunction (@sensitive string sensitiveValue, string anyValue) {

}
