import ballerina.data.sql;

struct Employee {
    int id;
    string name;
}

public function main (string[] args) {
    testSelectWithUntainted(args);
}

public function testSelectWithUntainted(string[] args) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:DB.HSQLDB_FILE, "./target/tempdb/",
                                   0, "TEST_SQL_CONNECTOR", "SA", "", {maximumPoolSize:1});
    }

    table dt = testDB.select("SELECT  FirstName from Customers where registrationID = 1", null, null);
    while (dt.hasNext()) {
        var rs, _ = (Employee)dt.getNext();
        testFunction(rs.name);
    }
    testDB.close();
    return;
}

public function testFunction (string anyValue) {

}
