import ballerina.data.sql;

function testConnectionPoolProperties () (string firstName) {
    endpoint<sql:ClientConnector> testDB {}
    sql:ConnectionProperties properties = {url:"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR_INIT",
                      driverClassName:"org.hsqldb.jdbc.JDBCDriver", maximumPoolSize:1,
                      idleTimeout:600000, connectionTimeout:30000, autoCommit:true, maxLifetime:1800000,
                      minimumIdle:1, poolName:"testHSQLPool", isolateInternalQueries:false,
                      allowPoolSuspension:false, readOnly:false, validationTimeout:5000, leakDetectionThreshold:0,
                      connectionInitSql:"SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS",
                      transactionIsolation:"TRANSACTION_READ_COMMITTED", catalog:"PUBLIC",
                      connectionTestQuery:"SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS"};
    bind create sql:ClientConnector(sql:DB.HSQLDB_FILE, "", 0, "", "SA", "", properties) with testDB;
    table dt = testDB.selectQuery("SELECT  FirstName from Customers where registrationID = 1", null, null);
    var j, _ = <json>dt;
    firstName = j.toString();
    return;
}

function testConnectorWithDefaultPropertiesForListedDB () (string firstName) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:DB.HSQLDB_FILE, "./target/tempdb/", 0,
                                   "TEST_SQL_CONNECTOR_INIT", "SA", "", null);
    }
    table dt = testDB.selectQuery("SELECT  FirstName from Customers where registrationID = 1", null, null);
    var j, _ = <json>dt;
    firstName = j.toString();
    return;
}


function testConnectorWithDirectUrl () (string firstName) {
    endpoint<sql:ClientConnector> testDB {}
    sql:ConnectionProperties Properties = {url:"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR_INIT"};
    bind create sql:ClientConnector(sql:DB.GENERIC, "", 0, "", "SA", "", Properties) with testDB;
    table dt = testDB.selectQuery("SELECT  FirstName from Customers where registrationID = 1", null, null);
    var j, _ = <json>dt;
    firstName = j.toString();
    return;
}

function testConnectorWithDataSourceClass () (string firstName) {
    endpoint<sql:ClientConnector> testDB {}
    map propertiesMap = {"loginTimeout":109, "url":"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR_INIT"};
    sql:ConnectionProperties properties = {dataSourceClassName:"org.hsqldb.jdbc.JDBCDataSource",
                                              datasourceProperties:propertiesMap};
    bind create sql:ClientConnector(sql:DB.GENERIC, "", 0, "", "SA", "", properties) with testDB;
    table dt = testDB.selectQuery("SELECT  FirstName from Customers where registrationID = 1", null, null);
    var j, _ = <json>dt;
    firstName = j.toString();
    return;
}

function testConnectorWithDataSourceClassAndProps () (string firstName) {
    endpoint<sql:ClientConnector> testDB {}
    map propertiesMap = {"loginTimeout":109};
    sql:ConnectionProperties properties = {dataSourceClassName:"org.hsqldb.jdbc.JDBCDataSource",
                                              datasourceProperties:propertiesMap};
    bind create sql:ClientConnector(sql:DB.HSQLDB_FILE, "./target/tempdb/",
                                    0, "TEST_SQL_CONNECTOR_INIT", "SA", "", properties) with testDB;
    table dt = testDB.selectQuery("SELECT  FirstName from Customers where registrationID = 1", null, null);
    var j, _ = <json>dt;
    firstName = j.toString();
    testDB.close();
    return;
}

function testConnectorWithDataSourceClassWithoutURL () (string firstName) {
    endpoint<sql:ClientConnector> testDB {}
    sql:ConnectionProperties properties = {dataSourceClassName:"org.hsqldb.jdbc.JDBCDataSource"};
    bind create sql:ClientConnector(sql:DB.HSQLDB_FILE, "./target/tempdb/", 0,
                                    "TEST_SQL_CONNECTOR_INIT", "SA", "", properties) with testDB;
    table dt = testDB.selectQuery("SELECT  FirstName from Customers where registrationID = 1", null, null);
    var j, _ = <json>dt;
    firstName = j.toString();
    return;
}

function testConnectorWithDataSourceClassURLPriority () (string firstName) {
    endpoint<sql:ClientConnector> testDB {}
    map propertiesMap = {"url":"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR_INIT"};
    sql:ConnectionProperties properties = {dataSourceClassName:"org.hsqldb.jdbc.JDBCDataSource",
                                              datasourceProperties:propertiesMap};
    bind create sql:ClientConnector(sql:DB.HSQLDB_FILE, "./target/tempdb/", 0,
                                    "INVALID_DB_NAME", "SA", "", properties) with testDB;
    table dt = testDB.selectQuery("SELECT  FirstName from Customers where registrationID = 1", null, null);
    var j, _ = <json>dt;
    firstName = j.toString();
    return;
}
