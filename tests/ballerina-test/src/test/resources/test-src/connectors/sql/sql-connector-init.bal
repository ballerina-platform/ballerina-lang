import ballerina/sql;

sql:ConnectionProperties properties = {url:"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR_INIT",
                  driverClassName:"org.hsqldb.jdbc.JDBCDriver", maximumPoolSize:1,
                  idleTimeout:600000, connectionTimeout:30000, autoCommit:true, maxLifetime:1800000,
                  minimumIdle:1, poolName:"testHSQLPool", isolateInternalQueries:false,
                  allowPoolSuspension:false, readOnly:false, validationTimeout:5000, leakDetectionThreshold:0,
                  connectionInitSql:"SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS",
                  transactionIsolation:"TRANSACTION_READ_COMMITTED", catalog:"PUBLIC",
                  connectionTestQuery:"SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS"};

sql:ConnectionProperties Properties2 = {url:"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR_INIT"};

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

function testConnectionPoolProperties1 () returns (string) {
    endpoint sql:Client testDB {
        database: sql:DB_HSQLDB_FILE,
        host: "",
        port: 0,
        name: "",
        username: "SA",
        password: "",
        options: properties
    };


    string firstName;
    table dt = check testDB -> select("SELECT  FirstName from Customers where registrationID = 1", null, null);
    var j = check <json>dt;
    firstName = j.toString();
    _ = testDB -> close();
    return firstName;
}

function testConnectionPoolProperties2 () returns (string) {
    endpoint sql:Client testDB {
        username: "SA",
        options: properties
    };

    table dt = check testDB -> select("SELECT  FirstName from Customers where registrationID = 1", null, null);

    string firstName;
    var j = check <json>dt;
    firstName = j.toString();
    _ = testDB -> close();
    return firstName;
}

function testConnectionPoolProperties3 () returns (string) {
    endpoint sql:Client testDB {
        database: sql:DB_HSQLDB_FILE,
        host: "./target/tempdb/",
        name: "TEST_SQL_CONNECTOR_INIT",
        username: "SA"
    };

    table dt = check testDB -> select("SELECT  FirstName from Customers where registrationID = 1", null, null);
    string firstName;
    var j = check <json>dt;
    firstName = j.toString();
    _ = testDB -> close();
    return firstName;
}


function testConnectorWithDefaultPropertiesForListedDB () returns (string) {
    endpoint sql:Client testDB {
        database: sql:DB_HSQLDB_FILE,
        host: "./target/tempdb/",
        name: "TEST_SQL_CONNECTOR_INIT",
        username: "SA",
        password: "",
        options: {}
    };

    table dt = check testDB -> select("SELECT  FirstName from Customers where registrationID = 1", null, null);

    string firstName;
    var j = check <json>dt;
    firstName = j.toString();
    _ = testDB -> close();
    return firstName;
}

function testConnectorWithWorkers () returns (string) {
    endpoint sql:Client testDB {
        database: sql:DB_HSQLDB_FILE,
        host: "./target/tempdb/",
        name: "TEST_SQL_CONNECTOR_INIT",
        username: "SA",
        password: "",
        options: {}
    };

    worker w1 {
        int x = 0;
        json y;

	    table dt = check testDB -> select("SELECT  FirstName from Customers where registrationID = 1", null, null);
	    var j = check <json>dt;
	    string firstName = j.toString();
        _ = testDB -> close();
	    return firstName;
    }    
    worker w2 {
        int x = 10;
    }
}


function testConnectorWithDirectUrl () returns (string) {
    endpoint sql:Client testDB {
        username: "SA",
        options: Properties2
    };

    table dt = check testDB -> select("SELECT  FirstName from Customers where registrationID = 1", null, null);
    string firstName;
    var j = check <json>dt;
    firstName = j.toString();
    _ = testDB -> close();
    return firstName;
}

function testConnectorWithDataSourceClass () returns (string) {
    endpoint sql:Client testDB {
        database: sql:DB_GENERIC,
        username: "SA",
        options: properties3
    };

    table dt = check testDB -> select("SELECT  FirstName from Customers where registrationID = 1", null, null);
    string firstName;
    var j = check <json>dt;
    firstName = j.toString();
    _ = testDB -> close();
    return firstName;
}

function testConnectorWithDataSourceClassAndProps () returns (string) {
    endpoint sql:Client testDB {
        database: sql:DB_HSQLDB_FILE,
        host: "./target/tempdb/",
        name: "TEST_SQL_CONNECTOR_INIT",
        username: "SA",
        password: "",
        options: properties4
    };

    table dt = check testDB -> select("SELECT  FirstName from Customers where registrationID = 1", null, null);
    string firstName;
    var j = check <json>dt;
    firstName = j.toString();
    _ = testDB -> close();
    return firstName;
}

function testConnectorWithDataSourceClassWithoutURL () returns (string) {
    endpoint sql:Client testDB {
        database: sql:DB_HSQLDB_FILE,
        host: "./target/tempdb/",
        name: "TEST_SQL_CONNECTOR_INIT",
        username: "SA",
        password: "",
        options: properties5
    };

    table dt = check testDB -> select("SELECT  FirstName from Customers where registrationID = 1", null, null);
    string firstName;
    var j = check <json>dt;
    firstName = j.toString();
    _ = testDB -> close();
    return firstName;
}

function testConnectorWithDataSourceClassURLPriority () returns (string) {
    endpoint sql:Client testDB {
        database: sql:DB_HSQLDB_FILE,
        host: "./target/tempdb/",
        name: "INVALID_DB_NAME",
        username: "SA",
        password: "",
        options: properties6
    };

    table dt = check testDB -> select("SELECT  FirstName from Customers where registrationID = 1", null, null);
    string firstName;
    var j = check <json>dt;
    firstName = j.toString();
    _ = testDB -> close();
    return firstName;
}
