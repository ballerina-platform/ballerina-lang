import ballerinax/java.jdbc;

public function main(string... args) {
    testSelectWithUntaintedQuery(...args);
}

public function testSelectWithUntaintedQuery(string... args) {
    jdbc:Client testDB = new ({
        url: "jdbc:mysql://localhost:3306/testdb",
        username: "root",
        password: "root",
        poolOptions: { maximumPoolSize: 5 },
        dbOptions: { }
    });

    var dt = testDB->select("SELECT  FirstName from Customers where registrationID = 1", ());
    checkpanic testDB.stop();
    return;
}
