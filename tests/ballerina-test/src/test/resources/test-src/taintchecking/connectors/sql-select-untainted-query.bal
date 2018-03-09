import ballerina.data.sql;

public function main (string[] args) {
    testSelectWithUntaintedQuery(args);
}

public function testSelectWithUntaintedQuery(string[] args) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:DB.HSQLDB_FILE, "./target/tempdb/",
                                   0, "TEST_SQL_CONNECTOR", "SA", "", {maximumPoolSize:1});
    }

    table dt = testDB.select("SELECT  FirstName from Customers where registrationID = 1", null, null);
    testDB.close();
    return;
}
