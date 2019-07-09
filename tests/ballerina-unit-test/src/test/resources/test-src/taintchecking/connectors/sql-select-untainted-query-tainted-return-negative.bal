import ballerina/mysql;

type Employee record {
    int id;
    string name;
};

public function main(string... args) {
    testSelectWithUntaintedQueryProducingTaintedReturn(...args);
}

public function testSelectWithUntaintedQueryProducingTaintedReturn(string... args) {
    mysql:Client testDB = new({
        host:"localhost",
        port:3306,
        name:"testdb",
        username:"root",
        password:"root",
        poolOptions:{maximumPoolSize:5},
        dbOptions: {}
    });

    var output = testDB->select("SELECT  FirstName from Customers where registrationID = 1", Employee);
    if (output is table<Employee>) {
        while (output.hasNext()) {
            var rs = <Employee>output.getNext();
            testFunction(rs.name);
        }
    }
    checkpanic testDB.stop();
    return;
}

public function testFunction(@sensitive string anyValue) {

}

