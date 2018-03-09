import ballerina.data.sql;

struct Employee {
    int id;
    string name;
}

public function main (string[] args) {
    testSelectWithUntaintedQueryProducingTaintedReturnNegative(args);
}

public function testSelectWithUntaintedQueryProducingTaintedReturnNegative(string[] args) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:DB.HSQLDB_FILE, "./target/tempdb/",
                                   0, "TEST_SQL_CONNECTOR", "SA", "", {maximumPoolSize:1});
    }

    table dataTable = testDB.select("SELECT  FirstName from Customers where registrationID = 1", null, null);
    while (dataTable.hasNext()) {
        var rs, _ = (Employee)dataTable.getNext();
        testFunction(rs.name, rs.name);
    }
    testDB.close();
    return;
}

public function testFunction (@sensitive{} string sensitiveValue, string anyValue) {

}
