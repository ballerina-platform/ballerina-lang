import ballerina/mysql;

public function main(string... args) {
    testSelectWithUntaintedQuery(...args);
}

public function testSelectWithUntaintedQuery(string... args) {
    mysql:Client testDB = new ({
        host:"localhost",
        port:3306,
        name:"testdb",
        username:"root",
        password:"root",
        poolOptions:{maximumPoolSize:5},
        dbOptions: {}
    });

    var dt = testDB->select("SELECT  FirstName from Customers where registrationID = 1", ());
    checkpanic testDB.stop();
    return;
}
