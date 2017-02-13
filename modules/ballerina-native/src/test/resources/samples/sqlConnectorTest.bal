import ballerina.lang.datatable;
import ballerina.data.sql;

function testInsertTableData() (int) {
    map propertiesMap = {"jdbcUrl" : "jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);

    int insertCount = sql:ClientConnector.update(testDB,"Insert into Customers
        (firstName,lastName,registrationID,creditLimit,country) values ('James', 'Clerk', 2, 5000.75, 'USA')");
    return insertCount;
}

function testCreateTable() (int) {
    map propertiesMap = {"jdbcUrl" : "jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);

    int returnValue = sql:ClientConnector.update(testDB, "CREATE TABLE IF NOT EXISTS Students(studentID int,
                LastName varchar(255))");
    return returnValue;
}

function testUpdateTableData() (int) {
    map propertiesMap = {"jdbcUrl" : "jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);

    int updateCount = sql:ClientConnector.update(testDB, "Update Customers set country = 'UK' where registrationID = 1");
    return updateCount;
}

function testGeneratedKeyOnInsert() (string) {
    map propertiesMap = {"jdbcUrl" : "jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);

    int insertCount;
    string generatedID;

    insertCount,generatedID = sql:ClientConnector.updateWithGeneratedKeys(testDB,
        "insert into Customers (firstName,lastName,registrationID,creditLimit,country)
        values ('Mary', 'Williams', 3, 5000.75, 'USA')");
    return generatedID;
}

function testGeneratedKeyWithColumn() (string) {
    map propertiesMap = {"jdbcUrl" : "jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);

    int insertCount;
    string generatedID;
    string[] keyColumns;
    keyColumns = ["CUSTOMERID"];

    insertCount,generatedID = sql:ClientConnector.updateWithGeneratedKeys(testDB,
        "insert into Customers (firstName,lastName,registrationID,creditLimit,country)
        values ('Kathy', 'Williams', 4, 5000.75, 'USA')",keyColumns);
    return generatedID;
}

function testSelectData() (string) {
    map propertiesMap = {"jdbcUrl" : "jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);

    string firstName;
    datatable dt = sql:ClientConnector.select(testDB, "SELECT  FirstName from Customers where registrationID = 1");
    while (datatable:next(dt)) {
        firstName = datatable:getString(dt, 1);
    }
    return firstName;
}

function testCallProcedure() (string) {
    map propertiesMap = {"jdbcUrl" : "jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);

    sql:ClientConnector.call(testDB, "{call InsertPersonData(100,'James')}");

    string firstName;
    datatable dt = sql:ClientConnector.select(testDB, "SELECT  FirstName from Customers where registrationID = 100");
    while (datatable:next(dt)) {
        firstName = datatable:getString(dt, 1);
    }
    return firstName;
}

function testConnectorWithDataSource() (string) {
    map propertiesMap = {"dataSourceClassName"  :"org.hsqldb.jdbc.JDBCDataSource",
        "dataSource.user":"SA", "dataSource.password":"", "dataSource.loginTimeout":0,
        "dataSource.url":"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR"};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);

    string firstName;
    datatable dt = sql:ClientConnector.select(testDB, "SELECT  FirstName from Customers where registrationID = 1");
    while (datatable:next(dt)) {
        firstName = datatable:getString(dt, 1);
    }
    return firstName;
}

function testConnectionPoolProperties() (string) {
    map propertiesMap = {"jdbcUrl" : "jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        "driverClassName":"org.hsqldb.jdbcDriver", "username":"SA", "password":"", "maximumPoolSize":1,
        "idleTimeout":600000,"connectionTimeout":30000,"autoCommit":"true","maxLifetime":1800000,
        "minimumIdle":1,"poolName":"testHSQLPool","initializationFailTimeout":1,
        "isolateInternalQueries":"false","allowPoolSuspension":"false","readOnly":"false",
        "registerMbeans":"false","validationTimeout":5000,"leakDetectionThreshold":0,
        "connectionInitSql":"SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS",
        "transactionIsolation":"2","catalog":"PUBLIC",
        "connectionTestQuery":"SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS"};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);

    string firstName;
    datatable dt = sql:ClientConnector.select(testDB, "SELECT  FirstName from Customers where registrationID = 1");
    while (datatable:next(dt)) {
        firstName = datatable:getString(dt, 1);
    }
    return firstName;
}

