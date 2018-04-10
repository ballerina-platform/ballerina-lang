import ballerina/sql;

sql:ConnectionProperties properties = {url:"hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR_INIT",
                  driverClassName:"org.hsqldb.jdbc.JDBCDriver", maximumPoolSize:1,
                  idleTimeout:600000, connectionTimeout:30000, autoCommit:true, maxLifetime:1800000,
                  minimumIdle:1, poolName:"testHSQLPool", isolateInternalQueries:false,
                  allowPoolSuspension:false, readOnly:false, validationTimeout:5000, leakDetectionThreshold:0,
                  connectionInitSql:"SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS",
                  transactionIsolation:"TRANSACTION_READ_COMMITTED", catalog:"PUBLIC",
                  connectionTestQuery:"SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS"};

sql:ConnectionProperties Properties2 = {url:"hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR_INIT"};

map propertiesMap = {"loginTimeout":109, "url":"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR_INIT"};
sql:ConnectionProperties properties3 = {dataSourceClassName:"org.hsqldb.jdbc.JDBCDataSource",
                                          datasourceProperties:propertiesMap};

map propertiesMap2 = {"loginTimeout":109};
sql:ConnectionProperties properties4 = {dataSourceClassName:"org.hsqldb.jdbc.JDBCDataSource",
                                          datasourceProperties:propertiesMap2};

sql:ConnectionProperties properties5 = {dataSourceClassName:"org.hsqldb.jdbc.JDBCDataSource"};

map propertiesMap3 = {"url":"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR_INIT"};
sql:ConnectionProperties properties6 = {dataSourceClassName:"org.hsqldb.jdbc.JDBCDataSource",
                                          datasourceProperties:propertiesMap3};

function testConnectionPoolProperties1 () returns (json) {
    endpoint sql:Client testDB {
        username: "SA",
        password: "",
        options: properties
    };


    var dtRet = testDB -> select("SELECT  FirstName from Customers where registrationID = 1", (), ());
    table dt = check dtRet;

    var j = check <json>dt;
    _ = testDB -> close();
    return j;
}

function testConnectionPoolProperties2 () returns (json) {
    endpoint sql:Client testDB {
        username: "SA",
        options: properties
    };

    var dtRet = testDB -> select("SELECT  FirstName from Customers where registrationID = 1", (), ());
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

    var dtRet = testDB -> select("SELECT  FirstName from Customers where registrationID = 1", (), ());
    table dt = check dtRet;

    var j = check <json>dt;
    _ = testDB -> close();
    return j;
}


function testConnectorWithDefaultPropertiesForListedDB () returns (json) {
    endpoint sql:Client testDB {
        url: "hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR_INIT",
        username: "SA",
        options: {}
    };

    var dtRet = testDB -> select("SELECT  FirstName from Customers where registrationID = 1", (), ());
    table dt = check dtRet;

    var j = check <json>dt;
    _ = testDB -> close();
    return j;
}

function testConnectorWithWorkers () returns (json) {
    endpoint sql:Client testDB {
        url: "hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR_INIT",
        username: "SA",
        options: {}
    };

    worker w1 {
        int x = 0;
        json y;

	    var dtRet = testDB -> select("SELECT  FirstName from Customers where registrationID = 1", (), ());
        table dt = check dtRet;

	    var j = check <json>dt;
        _ = testDB -> close();
	    return j;
    }    
    worker w2 {
        int x = 10;
    }
}


function testConnectorWithDirectUrl () returns (json) {
    endpoint sql:Client testDB {
        username: "SA",
        options: Properties2
    };

    var dtRet = testDB -> select("SELECT  FirstName from Customers where registrationID = 1", (), ());
    table dt = check dtRet;

    var j = check <json>dt;
    _ = testDB -> close();
    return j;
}

function testConnectorWithDataSourceClass () returns (json) {
    endpoint sql:Client testDB {
        username: "SA",
        options: properties3
    };

    var dtRet = testDB -> select("SELECT  FirstName from Customers where registrationID = 1", (), ());
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
        options: properties4
    };

    var dtRet = testDB -> select("SELECT  FirstName from Customers where registrationID = 1", (), ());
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
        options: properties5
    };

    var dtRet = testDB -> select("SELECT  FirstName from Customers where registrationID = 1", (), ());
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
        options: properties6
    };

    var dtRet = testDB -> select("SELECT  FirstName from Customers where registrationID = 1", (), ());
    table dt = check dtRet;

    var j = check <json>dt;
    _ = testDB -> close();
    return j;
}
