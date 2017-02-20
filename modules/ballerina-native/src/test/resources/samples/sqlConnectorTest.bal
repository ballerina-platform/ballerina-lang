import ballerina.lang.datatables;
import ballerina.data.sql;

function testInsertTableData() (int) {
    map propertiesMap = {"jdbcUrl" : "jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);
    sql:Parameter[] parameters=[];

    int insertCount = sql:ClientConnector.update(testDB,"Insert into Customers
        (firstName,lastName,registrationID,creditLimit,country) values ('James', 'Clerk', 2, 5000.75, 'USA')",
            parameters);
    return insertCount;
}

function testCreateTable() (int) {
    map propertiesMap = {"jdbcUrl" : "jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);
    sql:Parameter[] parameters=[];

    int returnValue = sql:ClientConnector.update(testDB, "CREATE TABLE IF NOT EXISTS Students(studentID int,
                LastName varchar(255))", parameters);
    return returnValue;
}

function testUpdateTableData() (int) {
    map propertiesMap = {"jdbcUrl" : "jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);
    sql:Parameter[] parameters=[];

    int updateCount = sql:ClientConnector.update(testDB, "Update Customers set country = 'UK' where registrationID = 1",
            parameters);
    return updateCount;
}

function testGeneratedKeyOnInsert() (string) {
    map propertiesMap = {"jdbcUrl" : "jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);

    int insertCount;
    string generatedID;
    sql:Parameter[] parameters=[];

    insertCount,generatedID = sql:ClientConnector.updateWithGeneratedKeys(testDB,
        "insert into Customers (firstName,lastName,registrationID,creditLimit,country)
        values ('Mary', 'Williams', 3, 5000.75, 'USA')", parameters);
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
    sql:Parameter[] parameters=[];

    insertCount,generatedID = sql:ClientConnector.updateWithGeneratedKeys(testDB,
        "insert into Customers (firstName,lastName,registrationID,creditLimit,country)
        values ('Kathy', 'Williams', 4, 5000.75, 'USA')",keyColumns, parameters);
    return generatedID;
}

function testSelectData() (string) {
    map propertiesMap = {"jdbcUrl" : "jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);

    string firstName;
    sql:Parameter[] parameters=[];
    datatable dt = sql:ClientConnector.select(testDB, "SELECT  FirstName from Customers where registrationID = 1",
        parameters);
    while (datatables:next(dt)) {
        firstName = datatables:getString(dt, 1);
    }
    datatables:close(dt);
    return firstName;
}

function testCallProcedure() (string) {
    map propertiesMap = {"jdbcUrl" : "jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);
    sql:Parameter[] parameters=[];
    sql:ClientConnector.call(testDB, "{call InsertPersonData(100,'James')}",parameters);

    string firstName;
    datatable dt = sql:ClientConnector.select(testDB, "SELECT  FirstName from Customers where registrationID = 100",
        parameters);
    while (datatables:next(dt)) {
        firstName = datatables:getString(dt, 1);
    }
    datatables:close(dt);
    return firstName;
}

function testConnectorWithDataSource() (string) {
    map propertiesMap = {"dataSourceClassName"  :"org.hsqldb.jdbc.JDBCDataSource",
        "dataSource.user":"SA", "dataSource.password":"", "dataSource.loginTimeout":0,
        "dataSource.url":"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR"};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);

    string firstName;
    sql:Parameter[] parameters=[];
    datatable dt = sql:ClientConnector.select(testDB, "SELECT  FirstName from Customers where registrationID = 1",
        parameters);
    while (datatables:next(dt)) {
        firstName = datatables:getString(dt, 1);
    }
    datatables:close(dt);
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
    sql:Parameter[] parameters=[];
    datatable dt = sql:ClientConnector.select(testDB, "SELECT  FirstName from Customers where registrationID = 1",
        parameters);
    while (datatables:next(dt)) {
        firstName = datatables:getString(dt, 1);
    }
    datatables:close(dt);
    return firstName;
}

function testQueryParameters() (string) {
    map propertiesMap = {"jdbcUrl" : "jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
            "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);

    string firstName;
    sql:Parameter para1 = {sqlType:"integer", value:"1", direction:0};
    sql:Parameter[] parameters=[para1];
    datatable dt = sql:ClientConnector.select(testDB, "SELECT  FirstName from Customers where registrationID = ?",
        parameters);
    while (datatables:next(dt)) {
        firstName = datatables:getString(dt, 1);
    }
    datatables:close(dt);
    return firstName;
}

function testInsertTableDataWithParameters() (int) {
    map propertiesMap = {"jdbcUrl" : "jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);
	sql:Parameter para1 = {sqlType:"varchar", value:"Anne", direction:0};
	sql:Parameter para2 = {sqlType:"varchar", value:"James", direction:0};
	sql:Parameter para3 = {sqlType:"integer", value:"3", direction:0};
	sql:Parameter para4 = {sqlType:"double", value:"5000.75", direction:0};
	sql:Parameter para5 = {sqlType:"varchar", value:"UK", direction:0};
    sql:Parameter[] parameters=[para1,para2,para3,para4,para5];

	int insertCount = sql:ClientConnector.update(testDB,"Insert into Customers
        (firstName,lastName,registrationID,creditLimit,country) values (?,?,?,?,?)", parameters);
    return insertCount;
}

