import ballerina/lang.datatables;
import ballerina/data.sql;
import ballerina/lang.errors;

struct ResultCustomers {
    string FIRSTNAME;
}

struct ResultDataType {
    int INT_TYPE;
    int LONG_TYPE;
    float FLOAT_TYPE;
    float DOUBLE_TYPE;
}

struct ResultCount {
    int COUNTVAL;
}

struct ResultArrayType {
    map INT_ARRAY;
    map LONG_ARRAY;
    map DOUBLE_ARRAY;
    map BOOLEAN_ARRAY;
    map STRING_ARRAY;
    map FLOAT_ARRAY;
}

function testInsertTableData () (int) {
    map propertiesMap = {"jdbcUrl":"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
                        "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);

    sql:Parameter[] parameters = [];
    int insertCount = sql:ClientConnector.update (testDB, "Insert into Customers
        (firstName,lastName,registrationID,creditLimit,country) values ('James', 'Clerk', 2, 5000.75, 'USA')",
                                                  parameters);
    sql:ClientConnector.close (testDB);
    return insertCount;
}

function testCreateTable () (int) {
    map propertiesMap = {"jdbcUrl":"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
                        "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);

    sql:Parameter[] parameters = [];
    int returnValue = sql:ClientConnector.update (testDB,
              "CREATE TABLE IF NOT EXISTS Students(studentID int, LastName varchar(255))", parameters);
    sql:ClientConnector.close (testDB);
    return returnValue;
}

function testUpdateTableData () (int) {
    map propertiesMap = {"jdbcUrl":"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
                        "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);

    sql:Parameter[] parameters = [];
    int updateCount = sql:ClientConnector.update (testDB,
              "Update Customers set country = 'UK' where registrationID = 1", parameters);
    sql:ClientConnector.close (testDB);
    return updateCount;
}

function testGeneratedKeyOnInsert () (string) {
    map propertiesMap = {"jdbcUrl":"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
                        "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);

    int insertCount;
    string[] generatedID;
    sql:Parameter[] parameters = [];
    string[] keyColumns = [];
    insertCount, generatedID = sql:ClientConnector.updateWithGeneratedKeys (testDB,
                "insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                values ('Mary', 'Williams', 3, 5000.75, 'USA')", parameters, keyColumns);
    sql:ClientConnector.close (testDB);
    return generatedID[0];
}

function testGeneratedKeyWithColumn () (string) {
    map propertiesMap = {"jdbcUrl":"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
                        "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);

    int insertCount;
    string[] generatedID;
    string[] keyColumns;
    keyColumns = ["CUSTOMERID"];
    sql:Parameter[] parameters = [];
    insertCount, generatedID = sql:ClientConnector.updateWithGeneratedKeys (testDB,
                "insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                values ('Kathy', 'Williams', 4, 5000.75, 'USA')", parameters, keyColumns);
    sql:ClientConnector.close (testDB);
    return generatedID[0];
}

function testSelectData () (string firstName) {
    map propertiesMap = {"jdbcUrl":"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
                        "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);

    sql:Parameter[] parameters = [];
    datatable dt = sql:ClientConnector.select (testDB, "SELECT  FirstName from Customers where registrationID = 1",
                                               parameters);
    errors:TypeCastError err;
    ResultCustomers rs;
    while (datatables:hasNext(dt)) {
        any dataStruct = datatables:next(dt);
        rs, err = (ResultCustomers) dataStruct;
        firstName = rs.FIRSTNAME;
    }
    sql:ClientConnector.close (testDB);
    return;
}

function testSelectIntFloatData () (int int_type, int long_type, float float_type, float double_type) {
    map propertiesMap = {"jdbcUrl":"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
                        "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);

    sql:Parameter[] parameters = [];
    errors:TypeCastError err;
    ResultDataType rs;
    datatable dt = sql:ClientConnector.select (testDB, "SELECT  int_type, long_type, float_type, double_type
              from DataTypeTable where row_id = 1", parameters);
    while (datatables:hasNext(dt)) {
        any dataStruct = datatables:next(dt);
        rs, err = (ResultDataType) dataStruct;
        int_type = rs.INT_TYPE;
        long_type = rs.LONG_TYPE;
        float_type = rs.FLOAT_TYPE;
        double_type = rs.DOUBLE_TYPE;
    }
    sql:ClientConnector.close (testDB);
    return;
}

function testCallProcedure () (string firstName) {
    map propertiesMap = {"jdbcUrl":"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
                        "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);

    sql:Parameter[] parameters = [];
    sql:ClientConnector.call (testDB, "{call InsertPersonData(100,'James')}", parameters);
    datatable dt = sql:ClientConnector.select (testDB, "SELECT  FirstName from Customers where registrationID = 100",
                                               parameters);
    errors:TypeCastError err;
    ResultCustomers rs;
    while (datatables:hasNext(dt)) {
        any dataStruct = datatables:next(dt);
        rs, err = (ResultCustomers) dataStruct;
        firstName = rs.FIRSTNAME;
    }
    sql:ClientConnector.close (testDB);
    return;
}

function testCallProcedureWithResultSet () (string firstName) {
    map propertiesMap = {"jdbcUrl":"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
                        "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);

    sql:Parameter[] parameters = [];
    datatable dt = sql:ClientConnector.call (testDB, "{call SelectPersonData()}", parameters);
    errors:TypeCastError err;
    ResultCustomers rs;
    while (datatables:hasNext(dt)) {
        any dataStruct = datatables:next(dt);
        rs, err = (ResultCustomers) dataStruct;
        firstName = rs.FIRSTNAME;
    }
    sql:ClientConnector.close (testDB);
    return;
}

function testConnectorWithDataSource () (string firstName) {
    map propertiesMap = {"dataSourceClassName":"org.hsqldb.jdbc.JDBCDataSource",
                        "dataSource.user":"SA", "dataSource.password":"", "dataSource.loginTimeout":0,
                        "dataSource.url":"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR"};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);

    sql:Parameter[] parameters = [];
    datatable dt = sql:ClientConnector.select (testDB, "SELECT  FirstName from Customers where registrationID = 1",
                                               parameters);
    errors:TypeCastError err;
    ResultCustomers rs;
    while (datatables:hasNext(dt)) {
        any dataStruct = datatables:next(dt);
        rs, err = (ResultCustomers) dataStruct;
        firstName = rs.FIRSTNAME;
    }
    sql:ClientConnector.close (testDB);
    return;
}

function testConnectionPoolProperties () (string firstName) {
    map propertiesMap = {"jdbcUrl":"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
                        "driverClassName":"org.hsqldb.jdbcDriver", "username":"SA", "password":"", "maximumPoolSize":1,
                        "idleTimeout":600000, "connectionTimeout":30000, "autoCommit":"true", "maxLifetime":1800000,
                        "minimumIdle":1, "poolName":"testHSQLPool", "initializationFailTimeout":1,
                        "isolateInternalQueries":"false", "allowPoolSuspension":"false", "readOnly":"false",
                        "registerMbeans":"false", "validationTimeout":5000, "leakDetectionThreshold":0,
                        "connectionInitSql":"SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS",
                        "transactionIsolation":"2", "catalog":"PUBLIC",
                        "connectionTestQuery":"SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS"};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);

    sql:Parameter[] parameters = [];
    datatable dt = sql:ClientConnector.select (testDB, "SELECT  FirstName from Customers where registrationID = 1",
                                               parameters);
    errors:TypeCastError err;
    ResultCustomers rs;
    while (datatables:hasNext(dt)) {
        any dataStruct = datatables:next(dt);
        rs, err = (ResultCustomers) dataStruct;
        firstName = rs.FIRSTNAME;
    }
    sql:ClientConnector.close (testDB);
    return;
}

function testQueryParameters () (string firstName) {
    map propertiesMap = {"jdbcUrl":"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
                        "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);

    sql:Parameter para1 = {sqlType:"integer", value:1, direction:0};
    sql:Parameter[] parameters = [para1];
    datatable dt = sql:ClientConnector.select (testDB, "SELECT  FirstName from Customers where registrationID = ?",
                                               parameters);
    errors:TypeCastError err;
    ResultCustomers rs;
    while (datatables:hasNext(dt)) {
        any dataStruct = datatables:next(dt);
        rs, err = (ResultCustomers) dataStruct;
        firstName = rs.FIRSTNAME;
    }
    sql:ClientConnector.close (testDB);
    return;
}

function testInsertTableDataWithParameters () (int) {
    map propertiesMap = {"jdbcUrl":"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
                        "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);
    sql:Parameter para1 = {sqlType:"varchar", value:"Anne", direction:0};
    sql:Parameter para2 = {sqlType:"varchar", value:"James", direction:0};
    sql:Parameter para3 = {sqlType:"integer", value:3, direction:0};
    sql:Parameter para4 = {sqlType:"double", value:5000.75, direction:0};
    sql:Parameter para5 = {sqlType:"varchar", value:"UK", direction:0};
    sql:Parameter[] parameters = [para1, para2, para3, para4, para5];

    int insertCount = sql:ClientConnector.update (testDB, "Insert into Customers
        (firstName,lastName,registrationID,creditLimit,country) values (?,?,?,?,?)", parameters);
    sql:ClientConnector.close (testDB);
    return insertCount;
}

function testOutParameters () (any, any, any, any, any, any, any, any, any, any, any, any, any, any) {
    map propertiesMap = {"jdbcUrl":"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
                        "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);

    sql:Parameter paraID = {sqlType:"integer", value:"1", direction:0};
    sql:Parameter paraInt = {sqlType:"integer", direction:1};
    sql:Parameter paraLong = {sqlType:"bigint", direction:1};
    sql:Parameter paraFloat = {sqlType:"float", direction:1};
    sql:Parameter paraDouble = {sqlType:"double", direction:1};
    sql:Parameter paraBool = {sqlType:"boolean", direction:1};
    sql:Parameter paraString = {sqlType:"varchar", direction:1};
    sql:Parameter paraNumeric = {sqlType:"numeric", direction:1};
    sql:Parameter paraDecimal = {sqlType:"decimal", direction:1};
    sql:Parameter paraReal = {sqlType:"real", direction:1};
    sql:Parameter paraTinyInt = {sqlType:"tinyint", direction:1};
    sql:Parameter paraSmallInt = {sqlType:"smallint", direction:1};
    sql:Parameter paraClob = {sqlType:"clob", direction:1};
    sql:Parameter paraBlob = {sqlType:"blob", direction:1};
    sql:Parameter paraBinary = {sqlType:"binary", direction:1};

    sql:Parameter[] parameters = [paraID, paraInt, paraLong, paraFloat, paraDouble, paraBool, paraString, paraNumeric,
                                  paraDecimal, paraReal, paraTinyInt, paraSmallInt, paraClob, paraBlob, paraBinary];
    sql:ClientConnector.call (testDB, "{call TestOutParams(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}", parameters);
    sql:ClientConnector.close (testDB);
    return paraInt.value, paraLong.value, paraFloat.value, paraDouble.value, paraBool.value, paraString.value,
           paraNumeric.value, paraDecimal.value, paraReal.value, paraTinyInt.value, paraSmallInt.value, paraClob.value,
           paraBlob.value, paraBinary.value;
}

function testNullOutParameters () (any, any, any, any, any, any, any, any, any, any, any, any, any, any) {
    map propertiesMap = {"jdbcUrl":"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
                        "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);

    sql:Parameter paraID = {sqlType:"integer", value:"2", direction:0};
    sql:Parameter paraInt = {sqlType:"integer", direction:1};
    sql:Parameter paraLong = {sqlType:"bigint", direction:1};
    sql:Parameter paraFloat = {sqlType:"float", direction:1};
    sql:Parameter paraDouble = {sqlType:"double", direction:1};
    sql:Parameter paraBool = {sqlType:"boolean", direction:1};
    sql:Parameter paraString = {sqlType:"varchar", direction:1};
    sql:Parameter paraNumeric = {sqlType:"numeric", direction:1};
    sql:Parameter paraDecimal = {sqlType:"decimal", direction:1};
    sql:Parameter paraReal = {sqlType:"real", direction:1};
    sql:Parameter paraTinyInt = {sqlType:"tinyint", direction:1};
    sql:Parameter paraSmallInt = {sqlType:"smallint", direction:1};
    sql:Parameter paraClob = {sqlType:"clob", direction:1};
    sql:Parameter paraBlob = {sqlType:"blob", direction:1};
    sql:Parameter paraBinary = {sqlType:"binary", direction:1};

    sql:Parameter[] parameters = [paraID, paraInt, paraLong, paraFloat, paraDouble, paraBool, paraString, paraNumeric,
                                  paraDecimal, paraReal, paraTinyInt, paraSmallInt, paraClob, paraBlob, paraBinary];
    sql:ClientConnector.call (testDB, "{call TestOutParams(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}", parameters);
    sql:ClientConnector.close (testDB);
    return paraInt.value, paraLong.value, paraFloat.value, paraDouble.value, paraBool.value, paraString.value,
           paraNumeric.value, paraDecimal.value, paraReal.value, paraTinyInt.value, paraSmallInt.value, paraClob.value,
           paraBlob.value, paraBinary.value;
}

function testINParameters () (int) {
    map propertiesMap = {"jdbcUrl":"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
                        "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);

    sql:Parameter paraID = {sqlType:"integer", value:3, direction:0};
    sql:Parameter paraInt = {sqlType:"integer", value:1, direction:0};
    sql:Parameter paraLong = {sqlType:"bigint", value:"9223372036854774807", direction:0};
    sql:Parameter paraFloat = {sqlType:"float", value:123.34, direction:0};
    sql:Parameter paraDouble = {sqlType:"double", value:2139095039, direction:0};
    sql:Parameter paraBool = {sqlType:"boolean", value:true, direction:0};
    sql:Parameter paraString = {sqlType:"varchar", value:"Hello", direction:0};
    sql:Parameter paraNumeric = {sqlType:"numeric", value:1234.567, direction:0};
    sql:Parameter paraDecimal = {sqlType:"decimal", value:1234.567, direction:0};
    sql:Parameter paraReal = {sqlType:"real", value:1234.567, direction:0};
    sql:Parameter paraTinyInt = {sqlType:"tinyint", value:1, direction:0};
    sql:Parameter paraSmallInt = {sqlType:"smallint", value:5555, direction:0};
    sql:Parameter paraClob = {sqlType:"clob", value:"very long text", direction:0};
    sql:Parameter paraBlob = {sqlType:"blob", value:"YmxvYiBkYXRh", direction:0};
    sql:Parameter paraBinary = {sqlType:"binary", value:"d3NvMiBiYWxsZXJpbmEgYmluYXJ5IHRlc3Qu", direction:0};

    sql:Parameter[] parameters = [paraID, paraInt, paraLong, paraFloat, paraDouble, paraBool, paraString, paraNumeric,
                                  paraDecimal, paraReal, paraTinyInt, paraSmallInt, paraClob, paraBlob, paraBinary];
    int insertCount = sql:ClientConnector.update (testDB, "INSERT INTO DataTypeTable (row_id,int_type, long_type,
            float_type, double_type, boolean_type, string_type, numeric_type, decimal_type, real_type, tinyint_type,
            smallint_type, clob_type, blob_type, binary_type) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", parameters);
    sql:ClientConnector.close (testDB);
    return insertCount;
}

function testNullINParameters () (int) {
    map propertiesMap = {"jdbcUrl":"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
                        "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);

    sql:Parameter paraID = {sqlType:"integer", value:4, direction:0};
    sql:Parameter paraInt = {sqlType:"integer", direction:0};
    sql:Parameter paraLong = {sqlType:"bigint", direction:0};
    sql:Parameter paraFloat = {sqlType:"float", direction:0};
    sql:Parameter paraDouble = {sqlType:"double", direction:0};
    sql:Parameter paraBool = {sqlType:"boolean", direction:0};
    sql:Parameter paraString = {sqlType:"varchar", direction:0};
    sql:Parameter paraNumeric = {sqlType:"numeric", direction:0};
    sql:Parameter paraDecimal = {sqlType:"decimal", direction:0};
    sql:Parameter paraReal = {sqlType:"real", direction:0};
    sql:Parameter paraTinyInt = {sqlType:"tinyint", direction:0};
    sql:Parameter paraSmallInt = {sqlType:"smallint", direction:0};
    sql:Parameter paraClob = {sqlType:"clob", direction:0};
    sql:Parameter paraBlob = {sqlType:"blob", direction:0};
    sql:Parameter paraBinary = {sqlType:"binary", direction:0};

    sql:Parameter[] parameters = [paraID, paraInt, paraLong, paraFloat, paraDouble, paraBool, paraString, paraNumeric,
                                  paraDecimal, paraReal, paraTinyInt, paraSmallInt, paraClob, paraBlob, paraBinary];
    int insertCount = sql:ClientConnector.update (testDB, "INSERT INTO DataTypeTable (row_id, int_type, long_type,
            float_type, double_type, boolean_type, string_type, numeric_type, decimal_type, real_type, tinyint_type,
            smallint_type, clob_type, blob_type, binary_type) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", parameters);
    sql:ClientConnector.close (testDB);
    return insertCount;
}

function testINOutParameters () (any, any, any, any, any, any, any, any, any, any, any, any, any, any) {
    map propertiesMap = {"jdbcUrl":"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
                        "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);

    sql:Parameter paraID = {sqlType:"integer", value:5, direction:0};
    sql:Parameter paraInt = {sqlType:"integer", value:10, direction:2};
    sql:Parameter paraLong = {sqlType:"bigint", value:"9223372036854774807", direction:2};
    sql:Parameter paraFloat = {sqlType:"float", value:123.34, direction:2};
    sql:Parameter paraDouble = {sqlType:"double", value:2139095039, direction:2};
    sql:Parameter paraBool = {sqlType:"boolean", value:true, direction:2};
    sql:Parameter paraString = {sqlType:"varchar", value:"Hello", direction:2};
    sql:Parameter paraNumeric = {sqlType:"numeric", value:1234.567, direction:2};
    sql:Parameter paraDecimal = {sqlType:"decimal", value:1234.567, direction:2};
    sql:Parameter paraReal = {sqlType:"real", value:1234.567, direction:2};
    sql:Parameter paraTinyInt = {sqlType:"tinyint", value:1, direction:2};
    sql:Parameter paraSmallInt = {sqlType:"smallint", value:5555, direction:2};
    sql:Parameter paraClob = {sqlType:"clob", value:"very long text", direction:2};
    sql:Parameter paraBlob = {sqlType:"blob", value:"YmxvYiBkYXRh", direction:2};
    sql:Parameter paraBinary = {sqlType:"binary", value:"d3NvMiBiYWxsZXJpbmEgYmluYXJ5IHRlc3Qu", direction:2};

    sql:Parameter[] parameters = [paraID, paraInt, paraLong, paraFloat, paraDouble, paraBool, paraString, paraNumeric,
                                  paraDecimal, paraReal, paraTinyInt, paraSmallInt, paraClob, paraBlob, paraBinary];
    sql:ClientConnector.call (testDB, "{call TestINOUTParams(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}", parameters);
    sql:ClientConnector.close (testDB);
    return paraInt.value, paraLong.value, paraFloat.value, paraDouble.value, paraBool.value, paraString.value,
           paraNumeric.value, paraDecimal.value, paraReal.value, paraTinyInt.value, paraSmallInt.value, paraClob.value,
           paraBlob.value, paraBinary.value;
}

function testNullINOutParameters ()
(any, any, any, any, any, any, any, any, any, any, any, any, any, any) {
    map propertiesMap = {"jdbcUrl":"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
                        "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);

    sql:Parameter paraID = {sqlType:"integer", value:"6", direction:0};
    sql:Parameter paraInt = {sqlType:"integer", direction:2};
    sql:Parameter paraLong = {sqlType:"bigint", direction:2};
    sql:Parameter paraFloat = {sqlType:"float", direction:2};
    sql:Parameter paraDouble = {sqlType:"double", direction:2};
    sql:Parameter paraBool = {sqlType:"boolean", direction:2};
    sql:Parameter paraString = {sqlType:"varchar", direction:2};
    sql:Parameter paraNumeric = {sqlType:"numeric", direction:2};
    sql:Parameter paraDecimal = {sqlType:"decimal", direction:2};
    sql:Parameter paraReal = {sqlType:"real", direction:2};
    sql:Parameter paraTinyInt = {sqlType:"tinyint", direction:2};
    sql:Parameter paraSmallInt = {sqlType:"smallint", direction:2};
    sql:Parameter paraClob = {sqlType:"clob", direction:2};
    sql:Parameter paraBlob = {sqlType:"blob", direction:2};
    sql:Parameter paraBinary = {sqlType:"binary", direction:2};

    sql:Parameter[] parameters = [paraID, paraInt, paraLong, paraFloat, paraDouble, paraBool, paraString, paraNumeric,
                                  paraDecimal, paraReal, paraTinyInt, paraSmallInt, paraClob, paraBlob, paraBinary];
    sql:ClientConnector.call (testDB, "{call TestINOUTParams(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}", parameters);
    sql:ClientConnector.close (testDB);
    return paraInt.value, paraLong.value, paraFloat.value, paraDouble.value, paraBool.value, paraString.value,
           paraNumeric.value, paraDecimal.value, paraReal.value, paraTinyInt.value, paraSmallInt.value, paraClob.value,
           paraBlob.value, paraBinary.value;
}

function testEmptySQLType () (int) {
    map propertiesMap = {"jdbcUrl":"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
                        "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);

    sql:Parameter para1 = {value:"Anne", direction:0};
    sql:Parameter[] parameters = [para1];
    int insertCount = sql:ClientConnector.update (testDB, "Insert into Customers (firstName) values (?)", parameters);
    sql:ClientConnector.close (testDB);
    return insertCount;
}

function testCloseConnectionPool () (int count) {
    map propertiesMap = {"jdbcUrl":"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
                        "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);

    sql:Parameter[] parameters = [];
    datatable dt = sql:ClientConnector.select (testDB, "SELECT COUNT(*) as countVal FROM INFORMATION_SCHEMA.SYSTEM_SESSIONS",
                                               parameters);
    errors:TypeCastError err;
    ResultCount rs;
    while (datatables:hasNext(dt)) {
        any dataStruct = datatables:next(dt);
        rs, err = (ResultCount) dataStruct;
        count = rs.COUNTVAL;
    }
    sql:ClientConnector.close (testDB);
    return;
}

function testArrayInParameters () (int insertCount, map int_arr, map long_arr, map double_arr, map string_arr,
                                   map boolean_arr, map float_arr) {
    map propertiesMap = {"jdbcUrl":"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
                        "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);

    sql:Parameter para1 = {sqlType:"integer", value:2, direction:0};
    sql:Parameter para2 = {sqlType:"array", value:"1", direction:0, structuredType:"integer"};
    sql:Parameter para3 = {sqlType:"array", value:"10000000, 20000000, 30000000", direction:0, structuredType:"bigint"};
    sql:Parameter para4 = {sqlType:"array", value:"245.23, 5559.49, 8796.123", direction:0, structuredType:"float"};
    sql:Parameter para5 = {sqlType:"array", value:"245.23, 5559.49, 8796.123", direction:0, structuredType:"double"};
    sql:Parameter para6 = {sqlType:"array", value:"TRUE, FALSE, TRUE", direction:0, structuredType:"boolean"};
    sql:Parameter para7 = {sqlType:"array", value:"Hello,Ballerina", direction:0, structuredType:"varchar"};
    sql:Parameter[] parameters = [para1, para2, para3, para4, para5, para6, para7];

    insertCount = sql:ClientConnector.update (testDB, "INSERT INTO ArrayTypes (row_id, int_array, long_array,
        float_array, double_array, boolean_array, string_array) values (?,?,?,?,?,?,?)", parameters);

    sql:Parameter[] params = [];
    datatable dt = sql:ClientConnector.select (testDB, "SELECT int_array, long_array, double_array, boolean_array,
        string_array, float_array from ArrayTypes where row_id = 2", params);
    ResultArrayType rs;
    while (datatables:hasNext(dt)) {
        any dataStruct = datatables:next(dt);
        rs, _ = (ResultArrayType) dataStruct;
        int_arr = rs.INT_ARRAY;
        long_arr = rs.LONG_ARRAY;
        double_arr = rs.DOUBLE_ARRAY;
        boolean_arr = rs.BOOLEAN_ARRAY;
        string_arr = rs.STRING_ARRAY;
        float_arr = rs.FLOAT_ARRAY;
    }
    sql:ClientConnector.close (testDB);
    return;
}

function testArrayOutParameters () (any, any, any, any, any, any) {
    map propertiesMap = {"jdbcUrl":"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
                        "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);

    string firstName;
    sql:Parameter para1 = {sqlType:"array", direction:1};
    sql:Parameter para2 = {sqlType:"array", direction:1};
    sql:Parameter para3 = {sqlType:"array", direction:1};
    sql:Parameter para4 = {sqlType:"array", direction:1};
    sql:Parameter para5 = {sqlType:"array", direction:1};
    sql:Parameter para6 = {sqlType:"array", direction:1};
    sql:Parameter[] parameters = [para1, para2, para3, para4, para5, para6];
    sql:ClientConnector.call (testDB, "{call TestArrayOutParams(?,?,?,?,?,?)}", parameters);
    sql:ClientConnector.close (testDB);
    return para1.value, para2.value, para3.value, para4.value, para5.value, para6.value;
}

function testArrayInOutParameters () (any, any, any, any, any, any, any) {
    map propertiesMap = {"jdbcUrl":"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
                        "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);

    sql:Parameter para1 = {sqlType:"integer", value:3, direction:0};
    sql:Parameter para2 = {sqlType:"integer", direction:1};
    sql:Parameter para3 = {sqlType:"array", value:"10,20,30", direction:2, structuredType:"integer"};
    sql:Parameter para4 = {sqlType:"array", value:"10000000, 20000000, 30000000", direction:2, structuredType:"bigint"};
    sql:Parameter para5 = {sqlType:"array", value:"2454.23, 55594.49, 87964.123", direction:2, structuredType:"float"};
    sql:Parameter para6 = {sqlType:"array", value:"2454.23, 55594.49, 87964.123", direction:2, structuredType:"double"};
    sql:Parameter para7 = {sqlType:"array", value:"FALSE, FALSE, TRUE", direction:2, structuredType:"boolean"};
    sql:Parameter para8 = {sqlType:"array", value:"Hello,Ballerina,Lang", direction:2, structuredType:"varchar"};
    sql:Parameter[] parameters = [para1, para2, para3, para4, para5, para6, para7, para8];

    sql:ClientConnector.call (testDB, "{call TestArrayInOutParams(?,?,?,?,?,?,?,?)}", parameters);
    sql:ClientConnector.close (testDB);
    return para2.value, para3.value, para4.value, para5.value, para6.value, para7.value, para8.value;
}

function testBatchUpdate () (int[]) {
    map propertiesMap = {"jdbcUrl":"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
                        "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);

    //Batch 1
    sql:Parameter para1 = {sqlType:"varchar", value:"Alex", direction:0};
    sql:Parameter para2 = {sqlType:"varchar", value:"Smith", direction:0};
    sql:Parameter para3 = {sqlType:"integer", value:20, direction:0};
    sql:Parameter para4 = {sqlType:"double", value:3400.5, direction:0};
    sql:Parameter para5 = {sqlType:"varchar", value:"Colombo", direction:0};
    sql:Parameter[] parameters1 = [para1, para2, para3, para4, para5];

    //Batch 2
    para1 = {sqlType:"varchar", value:"Alex", direction:0};
    para2 = {sqlType:"varchar", value:"Smith", direction:0};
    para3 = {sqlType:"integer", value:20, direction:0};
    para4 = {sqlType:"double", value:3400.5, direction:0};
    para5 = {sqlType:"varchar", value:"Colombo", direction:0};
    sql:Parameter[] parameters2 = [para1, para2, para3, para4, para5];
    sql:Parameter[][] parameters = [parameters1, parameters2];

    int[] updateCount;
    updateCount = sql:ClientConnector.batchUpdate(testDB, "Insert into Customers
                (firstName,lastName,registrationID,creditLimit,country) values (?,?,?,?,?)", parameters);
    sql:ClientConnector.close(testDB);
    return updateCount;
}

function testLocalTransacton () (int returnVal, int count) {
    map propertiesMap = {"jdbcUrl":"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
                            "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);
    returnVal = 0;
    sql:Parameter[] parameters = [];
    transaction {
        sql:ClientConnector.update(testDB, "Insert into Customers
                (firstName,lastName,registrationID,creditLimit,country) values ('James', 'Clerk', 200, 5000.75, 'USA')",
                                   parameters);
        sql:ClientConnector.update(testDB, "Insert into Customers
                (firstName,lastName,registrationID,creditLimit,country) values ('James', 'Clerk', 200, 5000.75, 'USA')",
                                   parameters);
    } aborted {
        returnVal = -1;
    }
    //check whether update action is performed
    datatable dt = sql:ClientConnector.select(testDB, "Select COUNT(*) as countval from Customers where
                                   registrationID = 200", parameters);
    errors:TypeCastError err;
    ResultCount rs;
    while (datatables:hasNext(dt)) {
        any dataStruct = datatables:next(dt);
        rs, err = (ResultCount)dataStruct;
        count = rs.COUNTVAL;
    }
    sql:ClientConnector.close(testDB);
    return;
}

function testTransactonRollback () (int returnVal, int count) {
    map propertiesMap = {"jdbcUrl":"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
                            "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);
    returnVal = 0;
    sql:Parameter[] parameters = [];
    try {
        transaction {
            sql:ClientConnector.update(testDB, "Insert into Customers (firstName,lastName,registrationID,
                creditLimit,country) values ('James', 'Clerk', 210, 5000.75, 'USA')", parameters);
            sql:ClientConnector.update(testDB, "Insert into Customers2 (firstName,lastName,registrationID,
                creditLimit,country) values ('James', 'Clerk', 210, 5000.75, 'USA')", parameters);
        } aborted {
            returnVal = -1;
        }
    } catch (errors:Error e) {
        // ignore.
    }
    //check whether update action is performed
    datatable dt = sql:ClientConnector.select(testDB, "Select COUNT(*) as countval from Customers where
                                   registrationID = 210", parameters);
    errors:TypeCastError err;
    ResultCount rs;
    while (datatables:hasNext(dt)) {
        any dataStruct = datatables:next(dt);
        rs, err = (ResultCount)dataStruct;
        count = rs.COUNTVAL;
    }
    sql:ClientConnector.close(testDB);
    return;
}

function testTransactonAbort () (int returnVal, int count) {
    map propertiesMap = {"jdbcUrl":"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
                            "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);
    returnVal = 0;
    sql:Parameter[] parameters = [];
    transaction {
        int insertCount = sql:ClientConnector.update(testDB, "Insert into Customers
                (firstName,lastName,registrationID,creditLimit,country) values ('James', 'Clerk', 220, 5000.75, 'USA')",
                                                     parameters);

        insertCount = sql:ClientConnector.update(testDB, "Insert into Customers
                (firstName,lastName,registrationID,creditLimit,country) values ('James', 'Clerk', 220, 5000.75, 'USA')",
                                                 parameters);
        int i = 0;
        if (i == 0) {
            abort;
        }
    } aborted {
        returnVal = -1;
    }
    //check whether update action is performed
    datatable dt = sql:ClientConnector.select(testDB, "Select COUNT(*) as countval from Customers where
                                   registrationID = 220", parameters);
    errors:TypeCastError err;
    ResultCount rs;
    while (datatables:hasNext(dt)) {
        any dataStruct = datatables:next(dt);
        rs, err = (ResultCount)dataStruct;
        count = rs.COUNTVAL;
    }
    sql:ClientConnector.close(testDB);
    return;
}

function testTransactonErrorThrow () (int returnVal, int catchValue, int count) {
    map propertiesMap = {"jdbcUrl":"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
                            "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);
    returnVal = 0;
    catchValue = 0;
    sql:Parameter[] parameters = [];
    try {
        transaction {
            int insertCount = sql:ClientConnector.update(testDB, "Insert into Customers (firstName,lastName,
                      registrationID,creditLimit,country) values ('James', 'Clerk', 260, 5000.75, 'USA')", parameters);
            int i = 0;
            if (i == 0) {
                errors:Error err = {msg:"error"};
                throw err;
            }
        } aborted {
            returnVal = -1;
        }
    } catch (errors:Error err) {
        catchValue = -1;
    }
    //check whether update action is performed
    datatable dt = sql:ClientConnector.select(testDB, "Select COUNT(*) as countval from Customers where
                                   registrationID = 260", parameters);
    ResultCount rs;
    while (datatables:hasNext(dt)) {
        any dataStruct = datatables:next(dt);
        rs, _ = (ResultCount)dataStruct;
        count = rs.COUNTVAL;
    }
    sql:ClientConnector.close(testDB);
    return;
}

function testTransactionErrorThrowAndCatch () (int returnVal, int catchValue, int count) {
    map propertiesMap = {"jdbcUrl":"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
                            "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);
    returnVal = 0;
    catchValue = 0;
    sql:Parameter[] parameters = [];
    transaction {
        int insertCount = sql:ClientConnector.update(testDB, "Insert into Customers (firstName,lastName,registrationID,
                 creditLimit,country) values ('James', 'Clerk', 250, 5000.75, 'USA')", parameters);
        int i = 0;
        try {
            if (i == 0) {
                errors:Error err = {msg:"error"};
                throw err;
            }
        } catch (errors:Error err) {
            catchValue = -1;
        }
    } aborted {
        returnVal = -1;
    }
    //check whether update action is performed
    errors:TypeCastError err;
    ResultCount rs;
    datatable dt = sql:ClientConnector.select(testDB, "Select COUNT(*) as countval from Customers where
                                   registrationID = 250", parameters);
    while (datatables:hasNext(dt)) {
        any dataStruct = datatables:next(dt);
        rs, err = (ResultCount)dataStruct;
        count = rs.COUNTVAL;
    }
    sql:ClientConnector.close(testDB);
    return;
}

function testTransactonCommitted () (int returnVal, int count) {
    map propertiesMap = {"jdbcUrl":"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
                            "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);
    returnVal = 0;
    sql:Parameter[] parameters = [];
    transaction {
        sql:ClientConnector.update(testDB, "Insert into Customers (firstName,lastName,registrationID,creditLimit,
               country) values ('James', 'Clerk', 300, 5000.75, 'USA')", parameters);
        sql:ClientConnector.update(testDB, "Insert into Customers (firstName,lastName,registrationID,creditLimit,
               country) values ('James', 'Clerk', 300, 5000.75, 'USA')", parameters);
    } committed {
        returnVal = 1;
    }
    //check whether update action is performed
    datatable dt = sql:ClientConnector.select(testDB, "Select COUNT(*) as countval from Customers where
                                  registrationID = 300", parameters);
    errors:TypeCastError err;
    ResultCount rs;
    while (datatables:hasNext(dt)) {
        any dataStruct = datatables:next(dt);
        rs, err = (ResultCount)dataStruct;
        count = rs.COUNTVAL;
    }
    sql:ClientConnector.close(testDB);
    return;
}

function testTransactonHandlerOrder () (int returnVal1, int returnVal2, int count) {
    map propertiesMap = {"jdbcUrl":"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
                            "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);
    returnVal1 = 0;
    returnVal2 = 0;
    sql:Parameter[] parameters = [];
    transaction {
        sql:ClientConnector.update(testDB, "Insert into Customers
            (firstName,lastName,registrationID,creditLimit,country) values ('James', 'Clerk', 400, 5000.75, 'USA')",
                                   parameters);
        sql:ClientConnector.update(testDB, "Insert into Customers
            (firstName,lastName,registrationID,creditLimit,country) values ('James', 'Clerk', 400, 5000.75, 'USA')",
                                   parameters);
    } committed {
        returnVal1 = 1;
    } aborted {
        returnVal1 = -1;
    }

    transaction {
        sql:ClientConnector.update(testDB, "Insert into Customers
            (firstName,lastName,registrationID,creditLimit,country) values ('James', 'Clerk', 400, 5000.75, 'USA')",
                                   parameters);
        sql:ClientConnector.update(testDB, "Insert into Customers
            (firstName,lastName,registrationID,creditLimit,country) values ('James', 'Clerk', 400, 5000.75, 'USA')",
                                   parameters);
    } aborted {
        returnVal2 = -1;
    } committed {
        returnVal2 = 1;
    }
    //check whether update action is performed
    datatable dt = sql:ClientConnector.select(testDB, "Select COUNT(*) as countval from Customers where
                                      registrationID = 400", parameters);
    ResultCount rs;
    while (datatables:hasNext(dt)) {
        any dataStruct = datatables:next(dt);
        rs, _ = (ResultCount)dataStruct;
        count = rs.COUNTVAL;
    }
    sql:ClientConnector.close(testDB);
    return returnVal1, returnVal2, count;
}

function testTransactonWithoutHandlers () (int count) {
    map propertiesMap = {"jdbcUrl":"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
                            "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);
    sql:Parameter[] parameters = [];
    transaction {
        sql:ClientConnector.update(testDB, "Insert into Customers
                        (firstName,lastName,registrationID,creditLimit,country) values
                                           ('James', 'Clerk', 350, 5000.75, 'USA')", parameters);
        sql:ClientConnector.update(testDB, "Insert into Customers
                        (firstName,lastName,registrationID,creditLimit,country) values
                                           ('James', 'Clerk', 350, 5000.75, 'USA')", parameters);
    }
    //check whether update action is performed
    datatable dt = sql:ClientConnector.select(testDB, "Select COUNT(*) as countval from Customers where
                                      registrationID = 350", parameters);
    ResultCount rs;
    while (datatables:hasNext(dt)) {
        any dataStruct = datatables:next(dt);
        rs, _ = (ResultCount)dataStruct;
        count = rs.COUNTVAL;
    }
    sql:ClientConnector.close(testDB);
    return;
}

function testDateTimeInParameters () (int[]) {
    map propertiesMap = {"jdbcUrl":"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
                            "username":"SA", "password":"", "maximumPoolSize":1};
    int[] returnValues = [];
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);
    sql:Parameter para1 = {sqlType:"integer", value:1, direction:0};
    sql:Parameter para2 = {sqlType:"date", value:"2017-01-30-08:01", direction:0};
    sql:Parameter para3 = {sqlType:"time", value:"13:27:01.999999+08:33", direction:0};
    sql:Parameter para4 = {sqlType:"timestamp", value:"2017-01-30T13:27:01.999-08:00", direction:0};
    sql:Parameter para5 = {sqlType:"datetime", value:"2017-01-30T13:27:01.999999Z", direction:0};
    sql:Parameter[] parameters = [para1, para2, para3, para4, para5];

    int insertCount1 = sql:ClientConnector.update(testDB, "Insert into DateTimeTypes
        (row_id,date_type,time_type,datetime_type, timestamp_type) values (?,?,?,?,?)", parameters);
    returnValues[0] = insertCount1;

    para1 = {sqlType:"integer", value:2, direction:0};
    para2 = {sqlType:"date", value:"-2017-01-30Z", direction:0};
    para3 = {sqlType:"time", value:"13:27:01+08:33", direction:0};
    para4 = {sqlType:"timestamp", value:"2017-01-30T13:27:01.999", direction:0};
    para5 = {sqlType:"datetime", value:"-2017-01-30T13:27:01.999999-08:30", direction:0};
    parameters = [para1, para2, para3, para4, para5];

    int insertCount2 = sql:ClientConnector.update(testDB, "Insert into DateTimeTypes
        (row_id,date_type,time_type,datetime_type, timestamp_type) values (?,?,?,?,?)", parameters);
    returnValues[1] = insertCount2;
    sql:ClientConnector.close(testDB);
    return returnValues;
}


function testDateTimeOutParams (int time, int date, int timestamp) (int count) {
    map propertiesMap = {"jdbcUrl":"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
                            "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);

    sql:Parameter para1 = {sqlType:"integer", value:10, direction:0};
    sql:Parameter para2 = {sqlType:"date", value:date, direction:0};
    sql:Parameter para3 = {sqlType:"time", value:time, direction:0};
    sql:Parameter para4 = {sqlType:"timestamp", value:timestamp, direction:0};
    sql:Parameter para5 = {sqlType:"datetime", value:timestamp, direction:0};

    sql:Parameter para6 = {sqlType:"date", direction:1};
    sql:Parameter para7 = {sqlType:"time", direction:1};
    sql:Parameter para8 = {sqlType:"timestamp", direction:1};
    sql:Parameter para9 = {sqlType:"datetime", direction:1};

    sql:Parameter[] parameters = [para1, para2, para3, para4, para5, para6, para7, para8, para9];

    sql:ClientConnector.call(testDB, "{call TestDateTimeOutParams(?,?,?,?,?,?,?,?,?)}", parameters);

    sql:Parameter[] emptyParam = [];
    datatable dt = sql:ClientConnector.select(testDB, "SELECT count(*) as countval from DateTimeTypes
                                  where row_id = 10", emptyParam);
    ResultCount rs;
    while (datatables:hasNext(dt)) {
        any dataStruct = datatables:next(dt);
        rs, _ = (ResultCount)dataStruct;
        count = rs.COUNTVAL;
    }
    sql:ClientConnector.close(testDB);
    return;
}