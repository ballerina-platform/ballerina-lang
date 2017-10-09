import ballerina.lang.datatables;
import ballerina.data.sql;

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
    sql:ClientConnector testDB = create sql:ClientConnector(sql:HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_SQL_CONNECTOR", "SA", "", {maximumPoolSize:1});

    sql:Parameter[] parameters = [];
    int insertCount = testDB.update ("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                                     values ('James', 'Clerk', 2, 5000.75, 'USA')", parameters);
    testDB.close ();
    return insertCount;
}

function testCreateTable () (int) {
    sql:ClientConnector testDB = create sql:ClientConnector(sql:HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_SQL_CONNECTOR", "SA", "", {maximumPoolSize:1});

    sql:Parameter[] parameters = [];
    int returnValue = testDB.update ("CREATE TABLE IF NOT EXISTS Students(studentID int, LastName varchar(255))",
                                     parameters);
    testDB.close ();
    return returnValue;
}

function testUpdateTableData () (int) {
    sql:ClientConnector testDB = create sql:ClientConnector(sql:HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_SQL_CONNECTOR", "SA", "", {maximumPoolSize:1});

    sql:Parameter[] parameters = [];
    int updateCount = testDB.update ("Update Customers set country = 'UK' where registrationID = 1", parameters);
    testDB.close ();
    return updateCount;
}

function testGeneratedKeyOnInsert () (string) {
    sql:ClientConnector testDB = create sql:ClientConnector(sql:HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_SQL_CONNECTOR", "SA", "", {maximumPoolSize:1});

    int insertCount;
    string[] generatedID;
    sql:Parameter[] parameters = [];
    string[] keyColumns = [];
    insertCount, generatedID = testDB.updateWithGeneratedKeys ("insert into Customers (firstName,lastName,
                             registrationID,creditLimit,country) values ('Mary', 'Williams', 3, 5000.75, 'USA')",
                                                               parameters, keyColumns);
    testDB.close ();
    return generatedID[0];
}

function testGeneratedKeyWithColumn () (string) {
    sql:ClientConnector testDB = create sql:ClientConnector(sql:HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_SQL_CONNECTOR", "SA", "", {maximumPoolSize:1});

    int insertCount;
    string[] generatedID;
    string[] keyColumns;
    keyColumns = ["CUSTOMERID"];
    sql:Parameter[] parameters = [];
    insertCount, generatedID = testDB.updateWithGeneratedKeys ("insert into Customers (firstName,lastName,
                               registrationID,creditLimit,country) values ('Kathy', 'Williams', 4, 5000.75, 'USA')",
                                                               parameters, keyColumns);
    testDB.close ();
    return generatedID[0];
}


function testInsertTableDataWithParameters () (int) {
    sql:ClientConnector testDB = create sql:ClientConnector(sql:HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_SQL_CONNECTOR", "SA", "", {maximumPoolSize:1});

    sql:Parameter para1 = {sqlType:"varchar", value:"Anne", direction:0};
    sql:Parameter para2 = {sqlType:"varchar", value:"James", direction:0};
    sql:Parameter para3 = {sqlType:"integer", value:3, direction:0};
    sql:Parameter para4 = {sqlType:"double", value:5000.75, direction:0};
    sql:Parameter para5 = {sqlType:"varchar", value:"UK", direction:0};
    sql:Parameter[] parameters = [para1, para2, para3, para4, para5];

    int insertCount = testDB.update ("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                                     values (?,?,?,?,?)", parameters);
    testDB.close ();
    return insertCount;
}

function testOutParameters () (any, any, any, any, any, any, any, any, any, any, any, any, any, any) {
    sql:ClientConnector testDB = create sql:ClientConnector(sql:HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_SQL_CONNECTOR", "SA", "", {maximumPoolSize:1});

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
    testDB.call ("{call TestOutParams(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}", parameters);
    testDB.close ();
    return paraInt.value, paraLong.value, paraFloat.value, paraDouble.value, paraBool.value, paraString.value,
           paraNumeric.value, paraDecimal.value, paraReal.value, paraTinyInt.value, paraSmallInt.value, paraClob.value,
           paraBlob.value, paraBinary.value;
}

function testNullOutParameters () (any, any, any, any, any, any, any, any, any, any, any, any, any, any) {
    sql:ClientConnector testDB = create sql:ClientConnector(sql:HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_SQL_CONNECTOR", "SA", "", {maximumPoolSize:1});

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
    testDB.call ("{call TestOutParams(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}", parameters);
    testDB.close ();
    return paraInt.value, paraLong.value, paraFloat.value, paraDouble.value, paraBool.value, paraString.value,
           paraNumeric.value, paraDecimal.value, paraReal.value, paraTinyInt.value, paraSmallInt.value, paraClob.value,
           paraBlob.value, paraBinary.value;
}

function testINParameters () (int) {
    sql:ClientConnector testDB = create sql:ClientConnector(sql:HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_SQL_CONNECTOR", "SA", "", {maximumPoolSize:1});

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
    int insertCount = testDB.update ("INSERT INTO DataTypeTable (row_id,int_type, long_type,
            float_type, double_type, boolean_type, string_type, numeric_type, decimal_type, real_type, tinyint_type,
            smallint_type, clob_type, blob_type, binary_type) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", parameters);
    testDB.close ();
    return insertCount;
}

function testNullINParameters () (int) {
    sql:ClientConnector testDB = create sql:ClientConnector(sql:HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_SQL_CONNECTOR", "SA", "", {maximumPoolSize:1});

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
    int insertCount = testDB.update ("INSERT INTO DataTypeTable (row_id, int_type, long_type,
            float_type, double_type, boolean_type, string_type, numeric_type, decimal_type, real_type, tinyint_type,
            smallint_type, clob_type, blob_type, binary_type) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", parameters);
    testDB.close ();
    return insertCount;
}

function testINOutParameters () (any, any, any, any, any, any, any, any, any, any, any, any, any, any) {
    sql:ClientConnector testDB = create sql:ClientConnector(sql:HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_SQL_CONNECTOR", "SA", "", {maximumPoolSize:1});

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
    testDB.call ("{call TestINOUTParams(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}", parameters);
    testDB.close ();
    return paraInt.value, paraLong.value, paraFloat.value, paraDouble.value, paraBool.value, paraString.value,
           paraNumeric.value, paraDecimal.value, paraReal.value, paraTinyInt.value, paraSmallInt.value, paraClob.value,
           paraBlob.value, paraBinary.value;
}

function testNullINOutParameters ()
(any, any, any, any, any, any, any, any, any, any, any, any, any, any) {
    sql:ClientConnector testDB = create sql:ClientConnector(sql:HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_SQL_CONNECTOR", "SA", "", {maximumPoolSize:1});

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
    testDB.call ("{call TestINOUTParams(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}", parameters);
    testDB.close ();
    return paraInt.value, paraLong.value, paraFloat.value, paraDouble.value, paraBool.value, paraString.value,
           paraNumeric.value, paraDecimal.value, paraReal.value, paraTinyInt.value, paraSmallInt.value, paraClob.value,
           paraBlob.value, paraBinary.value;
}

function testEmptySQLType () (int) {
    sql:ClientConnector testDB = create sql:ClientConnector(sql:HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_SQL_CONNECTOR", "SA", "", {maximumPoolSize:1});

    sql:Parameter para1 = {value:"Anne", direction:0};
    sql:Parameter[] parameters = [para1];
    int insertCount = testDB.update ("Insert into Customers (firstName) values (?)", parameters);
    testDB.close ();
    return insertCount;
}

function testArrayOutParameters () (any, any, any, any, any, any) {
    sql:ClientConnector testDB = create sql:ClientConnector(sql:HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_SQL_CONNECTOR", "SA", "", {maximumPoolSize:1});

    string firstName;
    sql:Parameter para1 = {sqlType:"array", direction:1};
    sql:Parameter para2 = {sqlType:"array", direction:1};
    sql:Parameter para3 = {sqlType:"array", direction:1};
    sql:Parameter para4 = {sqlType:"array", direction:1};
    sql:Parameter para5 = {sqlType:"array", direction:1};
    sql:Parameter para6 = {sqlType:"array", direction:1};
    sql:Parameter[] parameters = [para1, para2, para3, para4, para5, para6];
    testDB.call ("{call TestArrayOutParams(?,?,?,?,?,?)}", parameters);
    testDB.close ();
    return para1.value, para2.value, para3.value, para4.value, para5.value, para6.value;
}

function testArrayInOutParameters () (any, any, any, any, any, any, any) {
    sql:ClientConnector testDB = create sql:ClientConnector(sql:HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_SQL_CONNECTOR", "SA", "", {maximumPoolSize:1});

    sql:Parameter para1 = {sqlType:"integer", value:3, direction:0};
    sql:Parameter para2 = {sqlType:"integer", direction:1};
    sql:Parameter para3 = {sqlType:"array", value:"10,20,30", direction:2, structuredType:"integer"};
    sql:Parameter para4 = {sqlType:"array", value:"10000000, 20000000, 30000000", direction:2, structuredType:"bigint"};
    sql:Parameter para5 = {sqlType:"array", value:"2454.23, 55594.49, 87964.123", direction:2, structuredType:"float"};
    sql:Parameter para6 = {sqlType:"array", value:"2454.23, 55594.49, 87964.123", direction:2, structuredType:"double"};
    sql:Parameter para7 = {sqlType:"array", value:"FALSE, FALSE, TRUE", direction:2, structuredType:"boolean"};
    sql:Parameter para8 = {sqlType:"array", value:"Hello,Ballerina,Lang", direction:2, structuredType:"varchar"};
    sql:Parameter[] parameters = [para1, para2, para3, para4, para5, para6, para7, para8];

    testDB.call ("{call TestArrayInOutParams(?,?,?,?,?,?,?,?)}", parameters);
    testDB.close ();
    return para2.value, para3.value, para4.value, para5.value, para6.value, para7.value, para8.value;
}

function testBatchUpdate () (int[]) {
    sql:ClientConnector testDB = create sql:ClientConnector(sql:HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_SQL_CONNECTOR", "SA", "", {maximumPoolSize:1});

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
    updateCount = testDB.batchUpdate("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                                     values (?,?,?,?,?)", parameters);
    testDB.close();
    return updateCount;
}

function testDateTimeInParameters () (int[]) {
    sql:ClientConnector testDB = create sql:ClientConnector(sql:HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_SQL_CONNECTOR", "SA", "", {maximumPoolSize:1});
    int[] returnValues = [];
    sql:Parameter para1 = {sqlType:"integer", value:1, direction:0};
    sql:Parameter para2 = {sqlType:"date", value:"2017-01-30-08:01", direction:0};
    sql:Parameter para3 = {sqlType:"time", value:"13:27:01.999999+08:33", direction:0};
    sql:Parameter para4 = {sqlType:"timestamp", value:"2017-01-30T13:27:01.999-08:00", direction:0};
    sql:Parameter para5 = {sqlType:"datetime", value:"2017-01-30T13:27:01.999999Z", direction:0};
    sql:Parameter[] parameters = [para1, para2, para3, para4, para5];

    int insertCount1 = testDB.update("Insert into DateTimeTypes(row_id,date_type,time_type,datetime_type,timestamp_type)
                                     values (?,?,?,?,?)", parameters);
    returnValues[0] = insertCount1;

    para1 = {sqlType:"integer", value:2, direction:0};
    para2 = {sqlType:"date", value:"-2017-01-30Z", direction:0};
    para3 = {sqlType:"time", value:"13:27:01+08:33", direction:0};
    para4 = {sqlType:"timestamp", value:"2017-01-30T13:27:01.999", direction:0};
    para5 = {sqlType:"datetime", value:"-2017-01-30T13:27:01.999999-08:30", direction:0};
    parameters = [para1, para2, para3, para4, para5];

    int insertCount2 = testDB.update("Insert into DateTimeTypes(row_id,date_type,time_type,datetime_type,timestamp_type)
                                     values (?,?,?,?,?)", parameters);
    returnValues[1] = insertCount2;
    testDB.close();
    return returnValues;
}


function testInvalidDBType () (string firstName) {
    sql:ClientConnector testDB = create sql:ClientConnector("TESTDB", "./target/tempdb/",
                                                            0, "TEST_SQL_CONNECTOR", "SA", "", {maximumPoolSize:1});

    sql:Parameter[] parameters = [];
    testDB.update ("Insert into Customers(firstName) values ('James')",parameters);
    testDB.close ();
    return;
}
