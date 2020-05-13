import ballerinax/java.jdbc;

type Employee record {
    int id;
    string name;
};

public function main(string... args) {
    testSelectWithUntaintedQueryProducingTaintedReturn(...args);
}

public function testSelectWithUntaintedQueryProducingTaintedReturn(string... args) {
    jdbc:Client testDB = new ({
        url: "jdbc:mysql://localhost:3306/testdb",
        username: "root",
        password: "root",
        poolOptions: { maximumPoolSize: 5 },
        dbOptions: { }
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

public function testFunction(@untainted string anyValue) {

}

