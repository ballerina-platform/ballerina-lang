import ballerina/sql;

sql:PoolOptions properties = {maximumPoolSize:1,
                  idleTimeout:600000, connectionTimeout:30000, autoCommit:true, maxLifetime:1800000,
                  minimumIdle:1, validationTimeout:5000,
                  connectionInitSql:"SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS"};

map propertiesMap = {"loginTimeout":109};
sql:PoolOptions properties3 = {dataSourceClassName:"org.hsqldb.jdbc.JDBCDataSource",
    datasourceProperties:propertiesMap};

map propertiesMap2 = {"loginTimeout":109};
sql:PoolOptions properties4 = {dataSourceClassName:"org.hsqldb.jdbc.JDBCDataSource",
    datasourceProperties:propertiesMap2};

sql:PoolOptions properties5 = {dataSourceClassName:"org.hsqldb.jdbc.JDBCDataSource"};

map propertiesMap3 = {"url":"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR_INIT"};
sql:PoolOptions properties6 = {dataSourceClassName:"org.hsqldb.jdbc.JDBCDataSource",
    datasourceProperties:propertiesMap3};

function testConnectionPoolProperties1 () returns (json) {
    endpoint sql:Client testDB {
        url:"hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR_INIT",
        username: "SA",
        password: "",
        poolOptions: properties
    };


    var dtRet = testDB -> select("SELECT  FirstName from Customers where registrationID = 1", ());
    table dt = check dtRet;

    var j = check <json>dt;
    _ = testDB -> close();
    return j;
}

function testConnectionPoolProperties2 () returns (json) {
    endpoint sql:Client testDB {
        url:"hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR_INIT",
        username: "SA",
        poolOptions: properties
    };

    var dtRet = testDB -> select("SELECT  FirstName from Customers where registrationID = 1", ());
    table dt = check dtRet;

    var j = check <json>dt;
    _ = testDB -> close();
    return j;
}

function testConnectionPoolProperties3 () returns (json) {
    endpoint sql:Client testDB {
        url: "hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR_INIT",
        username: "SA"
    };

    var dtRet = testDB -> select("SELECT  FirstName from Customers where registrationID = 1", ());
    table dt = check dtRet;

    var j = check <json>dt;
    _ = testDB -> close();
    return j;
}


function testConnectorWithDefaultPropertiesForListedDB () returns (json) {
    endpoint sql:Client testDB {
        url: "hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR_INIT",
        username: "SA",
        poolOptions: {}
    };

    var dtRet = testDB -> select("SELECT  FirstName from Customers where registrationID = 1", ());
    table dt = check dtRet;

    var j = check <json>dt;
    _ = testDB -> close();
    return j;
}

function testConnectorWithWorkers () returns (json) {
    endpoint sql:Client testDB {
        url: "hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR_INIT",
        username: "SA",
        poolOptions: {}
    };

    worker w1 {
        int x = 0;
        json y;

	    var dtRet = testDB -> select("SELECT  FirstName from Customers where registrationID = 1", ());
        table dt = check dtRet;

	    var j = check <json>dt;
        _ = testDB -> close();
	    return j;
    }
    worker w2 {
        int x = 10;
    }
}

function testConnectorWithDataSourceClass () returns (json) {
    endpoint sql:Client testDB {
        url:"hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR_INIT",
        username: "SA",
        poolOptions: properties3
    };

    var dtRet = testDB -> select("SELECT  FirstName from Customers where registrationID = 1", ());
    table dt = check dtRet;

    var j = check <json>dt;
    _ = testDB -> close();
    return j;
}

function testConnectorWithDataSourceClassAndProps () returns (json) {
    endpoint sql:Client testDB {
        url: "hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR_INIT",
        username: "SA",
        password: "",
        poolOptions: properties4
    };

    var dtRet = testDB -> select("SELECT  FirstName from Customers where registrationID = 1", ());
    table dt = check dtRet;

    var j = check <json>dt;
    _ = testDB -> close();
    return j;
}

function testConnectorWithDataSourceClassWithoutURL () returns (json) {
    endpoint sql:Client testDB {
        url: "hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR_INIT",
        username: "SA",
        password: "",
        poolOptions: properties5
    };

    var dtRet = testDB -> select("SELECT  FirstName from Customers where registrationID = 1", ());
    table dt = check dtRet;

    var j = check <json>dt;
    _ = testDB -> close();
    return j;
}

function testConnectorWithDataSourceClassURLPriority () returns (json) {
    endpoint sql:Client testDB {
        url: "hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR_INIT",
        username: "SA",
        password: "",
        poolOptions: properties6
    };

    var dtRet = testDB -> select("SELECT  FirstName from Customers where registrationID = 1", ());
    table dt = check dtRet;

    var j = check <json>dt;
    _ = testDB -> close();
    return j;
}
