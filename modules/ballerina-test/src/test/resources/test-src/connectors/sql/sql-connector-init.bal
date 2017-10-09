import ballerina.data.sql;
import ballerina.lang.datatables;

struct ResultCustomers {
    string FIRSTNAME;
}


function testConnectorWithDataSource () (string firstName) {
    sql:ClientConnector testDB;
    map propertiesMap = {"user":"SA", "password":"", "loginTimeout":0,
                            "url":"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR"};
    sql:ConnectionProperties properties = {dataSourceClassName:"org.hsqldb.jdbc.JDBCDataSource",
                                              datasourceProperties:propertiesMap};
    testDB = create sql:ClientConnector(sql:HSQLDB_FILE, "", 0, "", "", "", properties);

    sql:Parameter[] parameters = [];
    datatable dt = testDB.select ("SELECT  FirstName from Customers where registrationID = 1", parameters);
    TypeCastError err;
    ResultCustomers rs;
    while (datatables:hasNext(dt)) {
        any dataStruct = datatables:getNext(dt);
        rs, err = (ResultCustomers) dataStruct;
        firstName = rs.FIRSTNAME;
    }
    testDB.close ();
    return;
}


function testConnectionPoolProperties () (string firstName) {
    sql:ClientConnector testDB;
    sql:ConnectionProperties properties = {url:"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
                                              driverClassName:"org.hsqldb.jdbcDriver", maximumPoolSize:1,
                                              idleTimeout:600000, connectionTimeout:30000, autoCommit:true, maxLifetime:1800000,
                                              minimumIdle:1, poolName:"testHSQLPool", isolateInternalQueries:false,
                                              allowPoolSuspension:false, readOnly:false, validationTimeout:5000, leakDetectionThreshold:0,
                                              connectionInitSql:"SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS",
                                              transactionIsolation:"TRANSACTION_READ_COMMITTED", catalog:"PUBLIC",
                                              connectionTestQuery:"SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS"};
    testDB = create sql:ClientConnector(sql:HSQLDB_FILE, "", 0, "", "SA", "", properties);

    sql:Parameter[] parameters = [];
    datatable dt = testDB.select ("SELECT  FirstName from Customers where registrationID = 1", parameters);
    TypeCastError err;
    ResultCustomers rs;
    while (datatables:hasNext(dt)) {
        any dataStruct = datatables:getNext(dt);
        rs, err = (ResultCustomers) dataStruct;
        firstName = rs.FIRSTNAME;
    }
    testDB.close ();
    return;
}
