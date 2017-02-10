import ballerina.data.sql;

function testInsertTable() (int) {
    map propertiesMap = {"jdbcUrl" : "jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        "username":"SA", "password":"", "maximumPoolSize":1};
    sql:Connector testDB = create sql:Connector(propertiesMap);

    int insertCount = sql:Connector.update(testDB,"Insert into Customers
        (firstName,lastName,registrationID,creditLimit,country) values ('James', 'Clerk', 2, 5000.75, 'USA')");
    return insertCount;
}
