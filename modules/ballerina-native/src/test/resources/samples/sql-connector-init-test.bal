import ballerina.data.sql;
import ballerina.lang.errors;
import ballerina.lang.datatables;

struct ResultCustomers {
    string FIRSTNAME;
}


function testConnectorWithDefaultPropertiesForListedDB () (string firstName) {
    sql:ClientConnector testDB = create sql:ClientConnector(sql:HSQLDB_FILE, "./target/tempdb/", 0,
                                                            "TEST_SQL_CONNECTOR", "SA", "", null);

    sql:Parameter[] parameters = [];
    datatable dt = testDB.select ("SELECT  FirstName from Customers where registrationID = 1", parameters);
    errors:TypeCastError err;
    ResultCustomers rs;
    while (datatables:hasNext(dt)) {
        any dataStruct = datatables:next(dt);
        rs, err = (ResultCustomers) dataStruct;
        firstName = rs.FIRSTNAME;
    }
    testDB.close ();
    return;
}

function testConnectorWithDirectUrl () (string firstName) {
    sql:ConnectionProperties Properties = {url:"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR"};
    sql:ClientConnector testDB = create sql:ClientConnector("", "", 0, "", "SA", "", Properties);

    sql:Parameter[] parameters = [];
    datatable dt = testDB.select ("SELECT  FirstName from Customers where registrationID = 1", parameters);
    errors:TypeCastError err;
    ResultCustomers rs;
    while (datatables:hasNext(dt)) {
        any dataStruct = datatables:next(dt);
        rs, err = (ResultCustomers) dataStruct;
        firstName = rs.FIRSTNAME;
    }
    testDB.close ();
    return;
}

function testConnectorWithDataSourceClass () (string firstName) {
    map propertiesMap = {"loginTimeout":109, "url":"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR"};
    sql:ConnectionProperties properties = {dataSourceClassName:"org.hsqldb.jdbc.JDBCDataSource",
                                              datasourceProperties:propertiesMap};
    sql:ClientConnector testDB = create sql:ClientConnector("", "", 0, "", "SA", "", properties);

    sql:Parameter[] parameters = [];
    datatable dt = testDB.select ("SELECT  FirstName from Customers where registrationID = 1", parameters);
    errors:TypeCastError err;
    ResultCustomers rs;
    while (datatables:hasNext(dt)) {
        any dataStruct = datatables:next(dt);
        rs, err = (ResultCustomers) dataStruct;
        firstName = rs.FIRSTNAME;
    }
    testDB.close ();
    return;
}

function testConnectorWithDataSourceClassWithoutURL () (string firstName) {
     sql:ConnectionProperties properties = {dataSourceClassName:"org.hsqldb.jdbc.JDBCDataSource"};
     sql:ClientConnector testDB = create sql:ClientConnector(sql:HSQLDB_FILE, "./target/tempdb/", 0,
                                                             "TEST_SQL_CONNECTOR", "SA", "", properties);

     sql:Parameter[] parameters = [];
     datatable dt = testDB.select ("SELECT  FirstName from Customers where registrationID = 1", parameters);
     errors:TypeCastError err;
     ResultCustomers rs;
     while (datatables:hasNext(dt)) {
         any dataStruct = datatables:next(dt);
         rs, err = (ResultCustomers) dataStruct;
         firstName = rs.FIRSTNAME;
     }
     testDB.close ();
     return;
 }

function testConnectorWithDataSourceClassURLPriority () (string firstName) {
    map propertiesMap = {"url":"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR"};
    sql:ConnectionProperties properties = {dataSourceClassName:"org.hsqldb.jdbc.JDBCDataSource",
                                              datasourceProperties:propertiesMap};
    sql:ClientConnector testDB = create sql:ClientConnector(sql:HSQLDB_FILE, "./target/tempdb/", 0,
                                                            "INVALID_DB_NAME", "SA", "", properties);

    sql:Parameter[] parameters = [];
    datatable dt = testDB.select ("SELECT  FirstName from Customers where registrationID = 1", parameters);
    errors:TypeCastError err;
    ResultCustomers rs;
    while (datatables:hasNext(dt)) {
        any dataStruct = datatables:next(dt);
        rs, err = (ResultCustomers) dataStruct;
        firstName = rs.FIRSTNAME;
    }
    testDB.close ();
    return;
}

function testConnectionPoolProperties () (string firstName) {
    sql:ConnectionProperties properties = {url:"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
                          driverClassName:"org.hsqldb.jdbcDriver", maximumPoolSize:1,
                          idleTimeout:600000, connectionTimeout:30000, autoCommit:true, maxLifetime:1800000,
                          minimumIdle:1, poolName:"testHSQLPool", isolateInternalQueries:false,
                          allowPoolSuspension:false, readOnly:false, validationTimeout:5000, leakDetectionThreshold:0,
                          connectionInitSql:"SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS",
                          transactionIsolation:"TRANSACTION_READ_COMMITTED", catalog:"PUBLIC",
                          connectionTestQuery:"SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS"};
    sql:ClientConnector testDB = create sql:ClientConnector("", "", 0, "", "SA", "", properties);

    sql:Parameter[] parameters = [];
    datatable dt = testDB.select ("SELECT  FirstName from Customers where registrationID = 1", parameters);
    errors:TypeCastError err;
    ResultCustomers rs;
    while (datatables:hasNext(dt)) {
        any dataStruct = datatables:next(dt);
        rs, err = (ResultCustomers) dataStruct;
        firstName = rs.FIRSTNAME;
    }
    testDB.close ();
    return;
}

function testInvalidDBType () {
    sql:ConnectionProperties properties = {maximumPoolSize:1};
    sql:ClientConnector testDB = create sql:ClientConnector("TESTDB", "./target/tempdb/", 0, "TEST_SQL_CONNECTOR", "SA",
                                                            "", properties);

    sql:Parameter[] parameters = [];
    testDB.update ("Insert into Customers(firstName) values ('James')",parameters);
    testDB.close ();
    return;
}