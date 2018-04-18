import ballerina/mysql;

function main (string... args) {
    testSelectWithTaintedQueryNegative(...args);
}

public function testSelectWithTaintedQueryNegative(string... args) {
    endpoint mysql:Client testDB {
        host: "localhost",
        port: 3306,
        name: "testdb",
        username: "root",
        password: "root",
        poolOptions: {maximumPoolSize:5}
    };

    var dt = testDB -> select("SELECT  FirstName from Customers where registrationID = " + args[0], null, null);
    var closeStatus = testDB -> close();
    return;
}
