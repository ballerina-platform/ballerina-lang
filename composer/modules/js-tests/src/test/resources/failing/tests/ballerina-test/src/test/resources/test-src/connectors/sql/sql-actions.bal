import ballerina.data.sql;
import ballerina.time;

struct ResultCustomers {
    string FIRSTNAME;
}

struct ResultIntType {
    int INT_TYPE;
}

struct ResultBlob {
    blob BLOB_TYPE;
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

struct ResultDates {
    string DATE_TYPE;
    string TIME_TYPE;
    string TIMESTAMP_TYPE;
    string DATETIME_TYPE;
}

function testInsertTableData () (int) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:DB.HSQLDB_FILE, "./target/tempdb/",
                                                           0, "TEST_SQL_CONNECTOR", "SA", "", {maximumPoolSize:1});
    }

    int insertCount = testDB.update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                                     values ('James', 'Clerk', 2, 5000.75, 'USA')", null);
    testDB.close();
    return insertCount;
}

function testCreateTable () (int) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:DB.HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_SQL_CONNECTOR", "SA", "", {maximumPoolSize:1});
    }

    int returnValue = testDB.update("CREATE TABLE IF NOT EXISTS Students(studentID int, LastName varchar(255))",
                                    null);
    testDB.close();
    return returnValue;
}

function testUpdateTableData () (int) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:DB.HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_SQL_CONNECTOR", "SA", "", {maximumPoolSize:1});
    }

    sql:Parameter[] parameters = [];
    int updateCount = testDB.update("Update Customers set country = 'UK' where registrationID = 1", parameters);
    testDB.close();
    return updateCount;
}

function testGeneratedKeyOnInsert () (string) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:DB.HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_SQL_CONNECTOR", "SA", "", {maximumPoolSize:1});
    }

    int insertCount;
    string[] generatedID;
    insertCount, generatedID = testDB.updateWithGeneratedKeys("insert into Customers (firstName,lastName,
                             registrationID,creditLimit,country) values ('Mary', 'Williams', 3, 5000.75, 'USA')",
                                                              null, null);
    testDB.close();
    return generatedID[0];
}

function testGeneratedKeyWithColumn () (string) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:DB.HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_SQL_CONNECTOR", "SA", "", {maximumPoolSize:1});
    }

    int insertCount;
    string[] generatedID;
    string[] keyColumns;
    keyColumns = ["CUSTOMERID"];
    insertCount, generatedID = testDB.updateWithGeneratedKeys("insert into Customers (firstName,lastName,
                               registrationID,creditLimit,country) values ('Kathy', 'Williams', 4, 5000.75, 'USA')",
                                                              null, keyColumns);
    testDB.close();
    return generatedID[0];
}


function testInsertTableDataWithParameters () (int) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:DB.HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_SQL_CONNECTOR", "SA", "", {maximumPoolSize:1});
    }

    sql:Parameter para1 = {sqlType:sql:Type.VARCHAR, value:"Anne", direction:sql:Direction.IN};
    sql:Parameter para2 = {sqlType:sql:Type.VARCHAR, value:"James", direction:sql:Direction.IN};
    sql:Parameter para3 = {sqlType:sql:Type.INTEGER, value:3, direction:sql:Direction.IN};
    sql:Parameter para4 = {sqlType:sql:Type.DOUBLE, value:5000.75, direction:sql:Direction.IN};
    sql:Parameter para5 = {sqlType:sql:Type.VARCHAR, value:"UK", direction:sql:Direction.IN};
    sql:Parameter[] parameters = [para1, para2, para3, para4, para5];

    int insertCount = testDB.update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                                     values (?,?,?,?,?)", parameters);
    testDB.close();
    return insertCount;
}

function testOutParameters () (any, any, any, any, any, any, any, any, any, any, any, any, any, any) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:DB.HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_SQL_CONNECTOR", "SA", "", {maximumPoolSize:1});
    }

    sql:Parameter paraID = {sqlType:sql:Type.INTEGER, value:"1"};
    sql:Parameter paraInt = {sqlType:sql:Type.INTEGER, direction:sql:Direction.OUT};
    sql:Parameter paraLong = {sqlType:sql:Type.BIGINT, direction:sql:Direction.OUT};
    sql:Parameter paraFloat = {sqlType:sql:Type.FLOAT, direction:sql:Direction.OUT};
    sql:Parameter paraDouble = {sqlType:sql:Type.DOUBLE, direction:sql:Direction.OUT};
    sql:Parameter paraBool = {sqlType:sql:Type.BOOLEAN, direction:sql:Direction.OUT};
    sql:Parameter paraString = {sqlType:sql:Type.VARCHAR, direction:sql:Direction.OUT};
    sql:Parameter paraNumeric = {sqlType:sql:Type.NUMERIC, direction:sql:Direction.OUT};
    sql:Parameter paraDecimal = {sqlType:sql:Type.DECIMAL, direction:sql:Direction.OUT};
    sql:Parameter paraReal = {sqlType:sql:Type.REAL, direction:sql:Direction.OUT};
    sql:Parameter paraTinyInt = {sqlType:sql:Type.TINYINT, direction:sql:Direction.OUT};
    sql:Parameter paraSmallInt = {sqlType:sql:Type.SMALLINT, direction:sql:Direction.OUT};
    sql:Parameter paraClob = {sqlType:sql:Type.CLOB, direction:sql:Direction.OUT};
    sql:Parameter paraBlob = {sqlType:sql:Type.BLOB, direction:sql:Direction.OUT};
    sql:Parameter paraBinary = {sqlType:sql:Type.BINARY, direction:sql:Direction.OUT};

    sql:Parameter[] parameters = [paraID, paraInt, paraLong, paraFloat, paraDouble, paraBool, paraString, paraNumeric,
                                  paraDecimal, paraReal, paraTinyInt, paraSmallInt, paraClob, paraBlob, paraBinary];
    _ = testDB.call("{call TestOutParams(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}", parameters, null);
    testDB.close();
    return paraInt.value, paraLong.value, paraFloat.value, paraDouble.value, paraBool.value, paraString.value,
           paraNumeric.value, paraDecimal.value, paraReal.value, paraTinyInt.value, paraSmallInt.value, paraClob.value,
           paraBlob.value, paraBinary.value;
}

function testNullOutParameters () (any, any, any, any, any, any, any, any, any, any, any, any, any, any) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:DB.HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_SQL_CONNECTOR", "SA", "", {maximumPoolSize:1});
    }

    sql:Parameter paraID = {sqlType:sql:Type.INTEGER, value:"2"};
    sql:Parameter paraInt = {sqlType:sql:Type.INTEGER, direction:sql:Direction.OUT};
    sql:Parameter paraLong = {sqlType:sql:Type.BIGINT, direction:sql:Direction.OUT};
    sql:Parameter paraFloat = {sqlType:sql:Type.FLOAT, direction:sql:Direction.OUT};
    sql:Parameter paraDouble = {sqlType:sql:Type.DOUBLE, direction:sql:Direction.OUT};
    sql:Parameter paraBool = {sqlType:sql:Type.BOOLEAN, direction:sql:Direction.OUT};
    sql:Parameter paraString = {sqlType:sql:Type.VARCHAR, direction:sql:Direction.OUT};
    sql:Parameter paraNumeric = {sqlType:sql:Type.NUMERIC, direction:sql:Direction.OUT};
    sql:Parameter paraDecimal = {sqlType:sql:Type.DECIMAL, direction:sql:Direction.OUT};
    sql:Parameter paraReal = {sqlType:sql:Type.REAL, direction:sql:Direction.OUT};
    sql:Parameter paraTinyInt = {sqlType:sql:Type.TINYINT, direction:sql:Direction.OUT};
    sql:Parameter paraSmallInt = {sqlType:sql:Type.SMALLINT, direction:sql:Direction.OUT};
    sql:Parameter paraClob = {sqlType:sql:Type.CLOB, direction:sql:Direction.OUT};
    sql:Parameter paraBlob = {sqlType:sql:Type.BLOB, direction:sql:Direction.OUT};
    sql:Parameter paraBinary = {sqlType:sql:Type.BINARY, direction:sql:Direction.OUT};

    sql:Parameter[] parameters = [paraID, paraInt, paraLong, paraFloat, paraDouble, paraBool, paraString, paraNumeric,
                                  paraDecimal, paraReal, paraTinyInt, paraSmallInt, paraClob, paraBlob, paraBinary];
    _ = testDB.call("{call TestOutParams(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}", parameters, null);
    testDB.close();
    return paraInt.value, paraLong.value, paraFloat.value, paraDouble.value, paraBool.value, paraString.value,
           paraNumeric.value, paraDecimal.value, paraReal.value, paraTinyInt.value, paraSmallInt.value, paraClob.value,
           paraBlob.value, paraBinary.value;
}

function testINParameters () (int) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:DB.HSQLDB_FILE, "./target/tempdb/",
                                                0, "TEST_SQL_CONNECTOR", "SA", "", {maximumPoolSize:1});
    }

    sql:Parameter paraID = {sqlType:sql:Type.INTEGER, value:3};
    sql:Parameter paraInt = {sqlType:sql:Type.INTEGER, value:1};
    sql:Parameter paraLong = {sqlType:sql:Type.BIGINT, value:"9223372036854774807"};
    sql:Parameter paraFloat = {sqlType:sql:Type.FLOAT, value:123.34};
    sql:Parameter paraDouble = {sqlType:sql:Type.DOUBLE, value:2139095039};
    sql:Parameter paraBool = {sqlType:sql:Type.BOOLEAN, value:true};
    sql:Parameter paraString = {sqlType:sql:Type.VARCHAR, value:"Hello"};
    sql:Parameter paraNumeric = {sqlType:sql:Type.NUMERIC, value:1234.567};
    sql:Parameter paraDecimal = {sqlType:sql:Type.DECIMAL, value:1234.567};
    sql:Parameter paraReal = {sqlType:sql:Type.REAL, value:1234.567};
    sql:Parameter paraTinyInt = {sqlType:sql:Type.TINYINT, value:1};
    sql:Parameter paraSmallInt = {sqlType:sql:Type.SMALLINT, value:5555};
    sql:Parameter paraClob = {sqlType:sql:Type.CLOB, value:"very long text"};
    sql:Parameter paraBlob = {sqlType:sql:Type.BLOB, value:"YmxvYiBkYXRh"};
    sql:Parameter paraBinary = {sqlType:sql:Type.BINARY, value:"d3NvMiBiYWxsZXJpbmEgYmluYXJ5IHRlc3Qu"};

    sql:Parameter[] parameters = [paraID, paraInt, paraLong, paraFloat, paraDouble, paraBool, paraString, paraNumeric,
                                  paraDecimal, paraReal, paraTinyInt, paraSmallInt, paraClob, paraBlob, paraBinary];
    int insertCount = testDB.update("INSERT INTO DataTypeTable (row_id,int_type, long_type,
            float_type, double_type, boolean_type, string_type, numeric_type, decimal_type, real_type, tinyint_type,
            smallint_type, clob_type, blob_type, binary_type) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", parameters);
    testDB.close();
    return insertCount;
}

function testNullINParameterValues () (int) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector( sql:DB.HSQLDB_FILE, "./target/tempdb/",
                                        0, "TEST_SQL_CONNECTOR", "SA", "", { maximumPoolSize:1});
    }

    sql:Parameter paraID = {sqlType:sql:Type.INTEGER, value:4};
    sql:Parameter paraInt = {sqlType:sql:Type.INTEGER};
    sql:Parameter paraLong = {sqlType:sql:Type.BIGINT};
    sql:Parameter paraFloat = {sqlType:sql:Type.FLOAT};
    sql:Parameter paraDouble = {sqlType:sql:Type.DOUBLE};
    sql:Parameter paraBool = {sqlType:sql:Type.BOOLEAN};
    sql:Parameter paraString = {sqlType:sql:Type.VARCHAR};
    sql:Parameter paraNumeric = {sqlType:sql:Type.NUMERIC};
    sql:Parameter paraDecimal = {sqlType:sql:Type.DECIMAL};
    sql:Parameter paraReal = {sqlType:sql:Type.REAL};
    sql:Parameter paraTinyInt = {sqlType:sql:Type.TINYINT};
    sql:Parameter paraSmallInt = {sqlType:sql:Type.SMALLINT};
    sql:Parameter paraClob = {sqlType:sql:Type.CLOB};
    sql:Parameter paraBlob = {sqlType:sql:Type.BLOB};
    sql:Parameter paraBinary = {sqlType:sql:Type.BINARY};

    sql:Parameter[] parameters = [paraID, paraInt, paraLong, paraFloat, paraDouble, paraBool, paraString, paraNumeric,
                                  paraDecimal, paraReal, paraTinyInt, paraSmallInt, paraClob, paraBlob, paraBinary];
    int insertCount = testDB.update("INSERT INTO DataTypeTable (row_id, int_type, long_type,
            float_type, double_type, boolean_type, string_type, numeric_type, decimal_type, real_type, tinyint_type,
            smallint_type, clob_type, blob_type, binary_type) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", parameters);
    testDB.close();
    return insertCount;
}

function testNullINParameters () (int) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector( sql:DB.HSQLDB_FILE, "./target/tempdb/",
                                                0, "TEST_SQL_CONNECTOR", "SA", "", { maximumPoolSize:1});
    }

    sql:Parameter paraID = {sqlType:sql:Type.INTEGER, value:10};

    sql:Parameter[] parameters = [paraID, null, null, null, null, null, null, null,
                                  null, null, null, null, null, null, null];
    int insertCount = testDB.update("INSERT INTO DataTypeTable (row_id,int_type, long_type,
                float_type, double_type, boolean_type, string_type, numeric_type, decimal_type, real_type, tinyint_type,
                smallint_type, clob_type, blob_type, binary_type) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", parameters);
    testDB.close();
    return insertCount;
}

function testINOutParameters () (any, any, any, any, any, any, any, any, any, any, any, any, any, any) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:DB.HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_SQL_CONNECTOR", "SA", "", {maximumPoolSize:1});
    }

    sql:Parameter paraID = {sqlType:sql:Type.INTEGER, value:5};
    sql:Parameter paraInt = {sqlType:sql:Type.INTEGER, value:10, direction:sql:Direction.INOUT};
    sql:Parameter paraLong = {sqlType:sql:Type.BIGINT, value:"9223372036854774807", direction:sql:Direction.INOUT};
    sql:Parameter paraFloat = {sqlType:sql:Type.FLOAT, value:123.34, direction:sql:Direction.INOUT};
    sql:Parameter paraDouble = {sqlType:sql:Type.DOUBLE, value:2139095039, direction:sql:Direction.INOUT};
    sql:Parameter paraBool = {sqlType:sql:Type.BOOLEAN, value:true, direction:sql:Direction.INOUT};
    sql:Parameter paraString = {sqlType:sql:Type.VARCHAR, value:"Hello", direction:sql:Direction.INOUT};
    sql:Parameter paraNumeric = {sqlType:sql:Type.NUMERIC, value:1234.567, direction:sql:Direction.INOUT};
    sql:Parameter paraDecimal = {sqlType:sql:Type.DECIMAL, value:1234.567, direction:sql:Direction.INOUT};
    sql:Parameter paraReal = {sqlType:sql:Type.REAL, value:1234.567, direction:sql:Direction.INOUT};
    sql:Parameter paraTinyInt = {sqlType:sql:Type.TINYINT, value:1, direction:sql:Direction.INOUT};
    sql:Parameter paraSmallInt = {sqlType:sql:Type.SMALLINT, value:5555, direction:sql:Direction.INOUT};
    sql:Parameter paraClob = {sqlType:sql:Type.CLOB, value:"very long text", direction:sql:Direction.INOUT};
    sql:Parameter paraBlob = {sqlType:sql:Type.BLOB, value:"YmxvYiBkYXRh", direction:sql:Direction.INOUT};
    sql:Parameter paraBinary = {sqlType:sql:Type.BINARY, value:"d3NvMiBiYWxsZXJpbmEgYmluYXJ5IHRlc3Qu", direction:sql:Direction.INOUT};

    sql:Parameter[] parameters = [paraID, paraInt, paraLong, paraFloat, paraDouble, paraBool, paraString, paraNumeric,
                                  paraDecimal, paraReal, paraTinyInt, paraSmallInt, paraClob, paraBlob, paraBinary];
    _ = testDB.call("{call TestINOUTParams(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}", parameters, null);
    testDB.close();
    return paraInt.value, paraLong.value, paraFloat.value, paraDouble.value, paraBool.value, paraString.value,
           paraNumeric.value, paraDecimal.value, paraReal.value, paraTinyInt.value, paraSmallInt.value, paraClob.value,
           paraBlob.value, paraBinary.value;
}

function testNullINOutParameters () (any, any, any, any, any, any, any, any, any, any, any, any, any, any) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:DB.HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_SQL_CONNECTOR", "SA", "", {maximumPoolSize:1});
    }

    sql:Parameter paraID = {sqlType:sql:Type.INTEGER, value:"6"};
    sql:Parameter paraInt = {sqlType:sql:Type.INTEGER, direction:sql:Direction.INOUT};
    sql:Parameter paraLong = {sqlType:sql:Type.BIGINT, direction:sql:Direction.INOUT};
    sql:Parameter paraFloat = {sqlType:sql:Type.FLOAT, direction:sql:Direction.INOUT};
    sql:Parameter paraDouble = {sqlType:sql:Type.DOUBLE, direction:sql:Direction.INOUT};
    sql:Parameter paraBool = {sqlType:sql:Type.BOOLEAN, direction:sql:Direction.INOUT};
    sql:Parameter paraString = {sqlType:sql:Type.VARCHAR, direction:sql:Direction.INOUT};
    sql:Parameter paraNumeric = {sqlType:sql:Type.NUMERIC, direction:sql:Direction.INOUT};
    sql:Parameter paraDecimal = {sqlType:sql:Type.DECIMAL, direction:sql:Direction.INOUT};
    sql:Parameter paraReal = {sqlType:sql:Type.REAL, direction:sql:Direction.INOUT};
    sql:Parameter paraTinyInt = {sqlType:sql:Type.TINYINT, direction:sql:Direction.INOUT};
    sql:Parameter paraSmallInt = {sqlType:sql:Type.SMALLINT, direction:sql:Direction.INOUT};
    sql:Parameter paraClob = {sqlType:sql:Type.CLOB, direction:sql:Direction.INOUT};
    sql:Parameter paraBlob = {sqlType:sql:Type.BLOB, direction:sql:Direction.INOUT};
    sql:Parameter paraBinary = {sqlType:sql:Type.BINARY, direction:sql:Direction.INOUT};

    sql:Parameter[] parameters = [paraID, paraInt, paraLong, paraFloat, paraDouble, paraBool, paraString, paraNumeric,
                                  paraDecimal, paraReal, paraTinyInt, paraSmallInt, paraClob, paraBlob, paraBinary];
    _ = testDB.call("{call TestINOUTParams(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}", parameters, null);
    testDB.close();
    return paraInt.value, paraLong.value, paraFloat.value, paraDouble.value, paraBool.value, paraString.value,
           paraNumeric.value, paraDecimal.value, paraReal.value, paraTinyInt.value, paraSmallInt.value, paraClob.value,
           paraBlob.value, paraBinary.value;
}

function testEmptySQLType () (int) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:DB.HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_SQL_CONNECTOR", "SA", "", {maximumPoolSize:1});
    }

    sql:Parameter para1 = {value:"Anne"};
    sql:Parameter[] parameters = [para1];
    int insertCount = testDB.update("Insert into Customers (firstName) values (?)", parameters);
    testDB.close();
    return insertCount;
}

function testArrayOutParameters () (any, any, any, any, any, any) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:DB.HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_SQL_CONNECTOR", "SA", "", {maximumPoolSize:1});
    }

    string firstName;
    sql:Parameter para1 = {sqlType:sql:Type.ARRAY, direction:sql:Direction.OUT};
    sql:Parameter para2 = {sqlType:sql:Type.ARRAY, direction:sql:Direction.OUT};
    sql:Parameter para3 = {sqlType:sql:Type.ARRAY, direction:sql:Direction.OUT};
    sql:Parameter para4 = {sqlType:sql:Type.ARRAY, direction:sql:Direction.OUT};
    sql:Parameter para5 = {sqlType:sql:Type.ARRAY, direction:sql:Direction.OUT};
    sql:Parameter para6 = {sqlType:sql:Type.ARRAY, direction:sql:Direction.OUT};
    sql:Parameter[] parameters = [para1, para2, para3, para4, para5, para6];
    _ = testDB.call("{call TestArrayOutParams(?,?,?,?,?,?)}", parameters, null);
    testDB.close();
    return para1.value, para2.value, para3.value, para4.value, para5.value, para6.value;
}

function testArrayInOutParameters () (any, any, any, any, any, any, any) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:DB.HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_SQL_CONNECTOR", "SA", "", {maximumPoolSize:1});
    }

    sql:Parameter para1 = {sqlType:sql:Type.INTEGER, value:3};
    sql:Parameter para2 = {sqlType:sql:Type.INTEGER, direction:sql:Direction.OUT};
    sql:Parameter para3 = {sqlType:sql:Type.ARRAY, value:"10,20,30", direction:sql:Direction.INOUT};
    sql:Parameter para4 = {sqlType:sql:Type.ARRAY, value:"10000000, 20000000, 30000000", direction:sql:Direction.INOUT};
    sql:Parameter para5 = {sqlType:sql:Type.ARRAY, value:"2454.23, 55594.49, 87964.123", direction:sql:Direction.INOUT};
    sql:Parameter para6 = {sqlType:sql:Type.ARRAY, value:"2454.23, 55594.49, 87964.123", direction:sql:Direction.INOUT};
    sql:Parameter para7 = {sqlType:sql:Type.ARRAY, value:"FALSE, FALSE, TRUE", direction:sql:Direction.INOUT};
    sql:Parameter para8 = {sqlType:sql:Type.ARRAY, value:"Hello,Ballerina,Lang", direction:sql:Direction.INOUT};
    sql:Parameter[] parameters = [para1, para2, para3, para4, para5, para6, para7, para8];

    _ = testDB.call("{call TestArrayInOutParams(?,?,?,?,?,?,?,?)}", parameters, null);
    testDB.close();
    return para2.value, para3.value, para4.value, para5.value, para6.value, para7.value, para8.value;
}

function testBatchUpdate () (int[]) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:DB.HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_SQL_CONNECTOR", "SA", "", {maximumPoolSize:1});
    }

    //Batch 1
    sql:Parameter para1 = {sqlType:sql:Type.VARCHAR, value:"Alex"};
    sql:Parameter para2 = {sqlType:sql:Type.VARCHAR, value:"Smith"};
    sql:Parameter para3 = {sqlType:sql:Type.INTEGER, value:20};
    sql:Parameter para4 = {sqlType:sql:Type.DOUBLE, value:3400.5};
    sql:Parameter para5 = {sqlType:sql:Type.VARCHAR, value:"Colombo"};
    sql:Parameter[] parameters1 = [para1, para2, para3, para4, para5];

    //Batch 2
    para1 = {sqlType:sql:Type.VARCHAR, value:"Alex"};
    para2 = {sqlType:sql:Type.VARCHAR, value:"Smith"};
    para3 = {sqlType:sql:Type.INTEGER, value:20};
    para4 = {sqlType:sql:Type.DOUBLE, value:3400.5};
    para5 = {sqlType:sql:Type.VARCHAR, value:"Colombo"};
    sql:Parameter[] parameters2 = [para1, para2, para3, para4, para5];
    sql:Parameter[][] parameters = [parameters1, parameters2];

    int[] updateCount;
    updateCount = testDB.batchUpdate("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                                     values (?,?,?,?,?)", parameters);
    testDB.close();
    return updateCount;
}

function testBatchUpdateWithFailure () (int[] updateCount, int count) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:DB.HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_SQL_CONNECTOR", "SA", "", {maximumPoolSize:1});
    }

    //Batch 1
    sql:Parameter para0 = {sqlType:sql:Type.INTEGER, value:111};
    sql:Parameter para1 = {sqlType:sql:Type.VARCHAR, value:"Alex"};
    sql:Parameter para2 = {sqlType:sql:Type.VARCHAR, value:"Smith"};
    sql:Parameter para3 = {sqlType:sql:Type.INTEGER, value:20};
    sql:Parameter para4 = {sqlType:sql:Type.DOUBLE, value:3400.5};
    sql:Parameter para5 = {sqlType:sql:Type.VARCHAR, value:"Colombo"};
    sql:Parameter[] parameters1 = [para0, para1, para2, para3, para4, para5];

    //Batch 2
    para0 = {sqlType:sql:Type.INTEGER, value:222};
    para1 = {sqlType:sql:Type.VARCHAR, value:"Alex"};
    para2 = {sqlType:sql:Type.VARCHAR, value:"Smith"};
    para3 = {sqlType:sql:Type.INTEGER, value:20};
    para4 = {sqlType:sql:Type.DOUBLE, value:3400.5};
    para5 = {sqlType:sql:Type.VARCHAR, value:"Colombo"};
    sql:Parameter[] parameters2 = [para0, para1, para2, para3, para4, para5];


    //Batch 3
    para0 = {sqlType:sql:Type.INTEGER, value:222};
    para1 = {sqlType:sql:Type.VARCHAR, value:"Alex"};
    para2 = {sqlType:sql:Type.VARCHAR, value:"Smith"};
    para3 = {sqlType:sql:Type.INTEGER, value:20};
    para4 = {sqlType:sql:Type.DOUBLE, value:3400.5};
    para5 = {sqlType:sql:Type.VARCHAR, value:"Colombo"};
    sql:Parameter[] parameters3 = [para0, para1, para2, para3, para4, para5];

    //Batch 4
    para0 = {sqlType:sql:Type.INTEGER, value:333};
    para1 = {sqlType:sql:Type.VARCHAR, value:"Alex"};
    para2 = {sqlType:sql:Type.VARCHAR, value:"Smith"};
    para3 = {sqlType:sql:Type.INTEGER, value:20};
    para4 = {sqlType:sql:Type.DOUBLE, value:3400.5};
    para5 = {sqlType:sql:Type.VARCHAR, value:"Colombo"};
    sql:Parameter[] parameters4 = [para0, para1, para2, para3, para4, para5];

    sql:Parameter[][] parameters = [parameters1, parameters2, parameters3, parameters4];

    updateCount = testDB.batchUpdate("Insert into Customers (customerId, firstName,lastName,registrationID,creditLimit,
        country) values (?,?,?,?,?,?)", parameters);
    table dt = testDB.select("SELECT count(*) as countval from Customers where customerId in (111,222,333)", null, typeof ResultCount);
    while (dt.hasNext()) {
        var rs, _ = (ResultCount)dt.getNext();
        count = rs.COUNTVAL;
    }

    testDB.close();
    return updateCount, count;
}

function testBatchUpdateWithNullParam () (int[]) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:DB.HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_SQL_CONNECTOR", "SA", "", {maximumPoolSize:1});
    }
    int[] updateCount;
    updateCount = testDB.batchUpdate("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                                     values ('Alex','Smith',20,3400.5,'Colombo')", null);
    testDB.close();
    return updateCount;
}

function testDateTimeInParameters () (int[]) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:DB.HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_SQL_CONNECTOR", "SA", "", {maximumPoolSize:1});
    }
    string stmt = "Insert into DateTimeTypes(row_id,date_type,time_type,datetime_type,timestamp_type) values (?,?,?,?,?)";
    int[] returnValues = [];
    sql:Parameter para1 = {sqlType:sql:Type.INTEGER, value:100};
    sql:Parameter para2 = {sqlType:sql:Type.DATE, value:"2017-01-30-08:01"};
    sql:Parameter para3 = {sqlType:sql:Type.TIME, value:"13:27:01.999999+08:33"};
    sql:Parameter para4 = {sqlType:sql:Type.TIMESTAMP, value:"2017-01-30T13:27:01.999-08:00"};
    sql:Parameter para5 = {sqlType:sql:Type.DATETIME, value:"2017-01-30T13:27:01.999999Z"};
    sql:Parameter[] parameters = [para1, para2, para3, para4, para5];

    int insertCount1 = testDB.update(stmt, parameters);
    returnValues[0] = insertCount1;

    para1 = {sqlType:sql:Type.INTEGER, value:200};
    para2 = {sqlType:sql:Type.DATE, value:"-2017-01-30Z"};
    para3 = {sqlType:sql:Type.TIME, value:"13:27:01+08:33"};
    para4 = {sqlType:sql:Type.TIMESTAMP, value:"2017-01-30T13:27:01.999"};
    para5 = {sqlType:sql:Type.DATETIME, value:"-2017-01-30T13:27:01.999999-08:30"};
    parameters = [para1, para2, para3, para4, para5];

    int insertCount2 = testDB.update(stmt, parameters);
    returnValues[1] = insertCount2;


    time:Time timeNow = time:currentTime();
    para1 = {sqlType:sql:Type.INTEGER, value:300};
    para2 = {sqlType:sql:Type.DATE, value:timeNow};
    para3 = {sqlType:sql:Type.TIME, value:timeNow};
    para4 = {sqlType:sql:Type.TIMESTAMP, value:timeNow};
    para5 = {sqlType:sql:Type.DATETIME, value:timeNow};
    parameters = [para1, para2, para3, para4, para5];

    int insertCount3 = testDB.update(stmt, parameters);
    returnValues[2] = insertCount3;

    testDB.close();
    return returnValues;
}

function testDateTimeNullInValues () (string data) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:DB.HSQLDB_FILE, "./target/tempdb/",
                                   0, "TEST_SQL_CONNECTOR", "SA", "", {maximumPoolSize:1});
    }
    sql:Parameter para0 = {sqlType:sql:Type.INTEGER, value:33};
    sql:Parameter para1 = {sqlType:sql:Type.DATE, value:null};
    sql:Parameter para2 = {sqlType:sql:Type.TIME, value:null};
    sql:Parameter para3 = {sqlType:sql:Type.TIMESTAMP, value:null};
    sql:Parameter para4 = {sqlType:sql:Type.DATETIME, value:null};
    sql:Parameter[] parameters = [para0, para1, para2, para3, para4];

    _ = testDB.update("Insert into DateTimeTypes
        (row_id, date_type, time_type, timestamp_type, datetime_type) values (?,?,?,?,?)", parameters);

    table dt = testDB.select("SELECT date_type, time_type, timestamp_type, datetime_type
                from DateTimeTypes where row_id = 33", null, typeof ResultDates);
    var j, _ = <json>dt;
    data = j.toString();

    testDB.close();
    return;
}


function testDateTimeNullOutValues () (int count) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:DB.HSQLDB_FILE, "./target/tempdb/",
                                   0, "TEST_SQL_CONNECTOR", "SA", "", {maximumPoolSize:1});
    }

    sql:Parameter para1 = {sqlType:sql:Type.INTEGER, value:123};
    sql:Parameter para2 = {sqlType:sql:Type.DATE, value:null};
    sql:Parameter para3 = {sqlType:sql:Type.TIME, value:null};
    sql:Parameter para4 = {sqlType:sql:Type.TIMESTAMP, value:null};
    sql:Parameter para5 = {sqlType:sql:Type.DATETIME, value:null};

    sql:Parameter para6 = {sqlType:sql:Type.DATE, direction:sql:Direction.OUT};
    sql:Parameter para7 = {sqlType:sql:Type.TIME, direction:sql:Direction.OUT};
    sql:Parameter para8 = {sqlType:sql:Type.TIMESTAMP, direction:sql:Direction.OUT};
    sql:Parameter para9 = {sqlType:sql:Type.DATETIME, direction:sql:Direction.OUT};

    sql:Parameter[] parameters = [para1, para2, para3, para4, para5, para6, para7, para8, para9];

    _ = testDB.call("{call TestDateTimeOutParams(?,?,?,?,?,?,?,?,?)}", parameters, null);

    table dt = testDB.select("SELECT count(*) as countval from DateTimeTypes where row_id = 123", null,
                                 typeof ResultCount);
    while (dt.hasNext()) {
        var rs, _ = (ResultCount)dt.getNext();
        count = rs.COUNTVAL;
    }
    testDB.close();
    return;
}

function testDateTimeNullInOutValues () (any, any, any, any) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:DB.HSQLDB_FILE, "./target/tempdb/",
                                   0, "TEST_SQL_CONNECTOR", "SA", "", {maximumPoolSize:1});
    }

    sql:Parameter para1 = {sqlType:sql:Type.INTEGER, value:124};
    sql:Parameter para2 = {sqlType:sql:Type.DATE, value:null, direction:sql:Direction.INOUT};
    sql:Parameter para3 = {sqlType:sql:Type.TIME, value:null, direction:sql:Direction.INOUT};
    sql:Parameter para4 = {sqlType:sql:Type.TIMESTAMP, value:null, direction:sql:Direction.INOUT};
    sql:Parameter para5 = {sqlType:sql:Type.DATETIME, value:null, direction:sql:Direction.INOUT};

    sql:Parameter[] parameters = [para1, para2, para3, para4, para5];

    _ = testDB.call("{call TestDateINOUTParams(?,?,?,?,?)}", parameters, null);
    testDB.close();
    return para2.value, para3.value, para4.value, para5.value;
}

function testSelectIntFloatData () (int int_type, int long_type, float float_type, float double_type) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:DB.HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_SQL_CONNECTOR", "SA", "", {maximumPoolSize:1});
    }
    table dt = testDB.select("SELECT  int_type, long_type, float_type, double_type from DataTypeTable
                                   where row_id = 1", null, typeof ResultDataType);
    while (dt.hasNext()) {
        var rs, err = (ResultDataType)dt.getNext();
        int_type = rs.INT_TYPE;
        long_type = rs.LONG_TYPE;
        float_type = rs.FLOAT_TYPE;
        double_type = rs.DOUBLE_TYPE;
    }
    testDB.close();
    return;
}

function testSelectData () (string firstName) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:DB.HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_SQL_CONNECTOR", "SA", "", {maximumPoolSize:1});
    }

    table dt = testDB.select("SELECT  FirstName from Customers where registrationID = 1", null,
                                 typeof ResultCustomers);
    while (dt.hasNext()) {
        var rs, err = (ResultCustomers)dt.getNext();
        firstName = rs.FIRSTNAME;
    }
    testDB.close();
    return;
}

function testCallProcedure () (string firstName) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:DB.HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_SQL_CONNECTOR", "SA", "", {maximumPoolSize:1});
    }

    _ = testDB.call("{call InsertPersonData(100,'James')}", null, null);
    table dt = testDB.select("SELECT  FirstName from Customers where registrationID = 100", null,
                                 typeof ResultCustomers);
    while (dt.hasNext()) {
        var rs, err = (ResultCustomers)dt.getNext();
        firstName = rs.FIRSTNAME;
    }
    testDB.close();
    return;
}

function testCallProcedureWithResultSet () (string firstName) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:DB.HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_SQL_CONNECTOR", "SA", "", {maximumPoolSize:1});
    }

    table[] dtArray = testDB.call("{call SelectPersonData()}", null, typeof ResultCustomers);
    while (dtArray[0].hasNext()) {
        var rs, err = (ResultCustomers)dtArray[0].getNext();
        firstName = rs.FIRSTNAME;
    }
    testDB.close();
    return;
}

function testQueryParameters () (string firstName) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:DB.HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_SQL_CONNECTOR", "SA", "", {maximumPoolSize:1});
    }

    sql:Parameter para1 = {sqlType:sql:Type.INTEGER, value:1};
    sql:Parameter[] parameters = [para1];
    table dt = testDB.select("SELECT  FirstName from Customers where registrationID = ?", parameters,
                                 typeof ResultCustomers);
    while (dt.hasNext()) {
        var rs, err = (ResultCustomers)dt.getNext();
        firstName = rs.FIRSTNAME;
    }
    testDB.close();
    return;
}

function testArrayofQueryParameters () (string firstName) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:DB.HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_SQL_CONNECTOR", "SA", "", {maximumPoolSize:1});
    }

    int[] intDataArray = [1, 4343];
    string[] stringDataArray = ["A", "B"];
    float[] doubleArray = [233.4, 433.4];
    sql:Parameter para0 = {sqlType:sql:Type.VARCHAR, value:"Johhhn"};
    sql:Parameter para1 = {sqlType:sql:Type.INTEGER, value:intDataArray};
    sql:Parameter para2 = {sqlType:sql:Type.VARCHAR, value:stringDataArray};
    sql:Parameter para3 = {sqlType:sql:Type.DOUBLE, value:doubleArray};
    sql:Parameter[] parameters = [para0, para1, para2, para3];
    table dt = testDB.select("SELECT  FirstName from Customers where FirstName = ? or lastName = 'A' or
                    lastName = '\"BB\"' or registrationID in(?) or lastName in(?) or creditLimit in(?)", parameters,
                                 typeof ResultCustomers);
    while (dt.hasNext()) {
        var rs, err = (ResultCustomers)dt.getNext();
        firstName = rs.FIRSTNAME;
    }
    testDB.close();
    return;
}

function testBoolArrayofQueryParameters () (int value) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:DB.HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_SQL_CONNECTOR", "SA", "", {maximumPoolSize:1});
    }
    boolean accepted1 = false;
    boolean accepted2 = false;
    boolean accepted3 = true;
    boolean[] boolDataArray = [accepted1, accepted2, accepted3];


    table dt1 = testDB.select("SELECT blob_type from DataTypeTable where row_id = 1", null, typeof ResultBlob);
    blob blobData;
    while (dt1.hasNext()) {
        var rs, err = (ResultBlob)dt1.getNext();
        blobData = rs.BLOB_TYPE;
    }
    blob[] blobDataArray = [blobData];

    sql:Parameter para0 = {sqlType:sql:Type.INTEGER, value:1};
    sql:Parameter para1 = {sqlType:sql:Type.BOOLEAN, value:boolDataArray};
    sql:Parameter para2 = {sqlType:sql:Type.BLOB, value:blobDataArray};
    sql:Parameter[] parameters = [para0, para1, para2];
    table dt = testDB.select("SELECT  int_type from DataTypeTable where row_id = ? and boolean_type in(?) and
                                                            blob_type in (?)", parameters, typeof ResultIntType);
    while (dt.hasNext()) {
        var rs, err = (ResultIntType)dt.getNext();
        value = rs.INT_TYPE;
    }
    testDB.close();
    return;
}


function testArrayInParameters () (int insertCount, map int_arr, map long_arr, map double_arr, map string_arr,
                                   map boolean_arr, map float_arr) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:DB.HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_SQL_CONNECTOR", "SA", "", {maximumPoolSize:1});
    }

    int[] intArray = [1];
    int[] longArray = [1503383034226, 1503383034224, 1503383034225];
    float[] floatArray = [245.23, 5559.49, 8796.123];
    float[] doubleArray = [1503383034226.23, 1503383034224.43, 1503383034225.123];
    boolean[] boolArray = [true, false, true];
    string[] stringArray = ["Hello", "Ballerina"];
    sql:Parameter para1 = {sqlType:sql:Type.INTEGER, value:2};
    sql:Parameter para2 = {sqlType:sql:Type.ARRAY, value:intArray};
    sql:Parameter para3 = {sqlType:sql:Type.ARRAY, value:longArray};
    sql:Parameter para4 = {sqlType:sql:Type.ARRAY, value:floatArray};
    sql:Parameter para5 = {sqlType:sql:Type.ARRAY, value:doubleArray};
    sql:Parameter para6 = {sqlType:sql:Type.ARRAY, value:boolArray};
    sql:Parameter para7 = {sqlType:sql:Type.ARRAY, value:stringArray};
    sql:Parameter[] parameters = [para1, para2, para3, para4, para5, para6, para7];

    insertCount = testDB.update("INSERT INTO ArrayTypes (row_id, int_array, long_array,
        float_array, double_array, boolean_array, string_array) values (?,?,?,?,?,?,?)", parameters);

    table dt = testDB.select("SELECT int_array, long_array, double_array, boolean_array,
        string_array, float_array from ArrayTypes where row_id = 2", null, typeof ResultArrayType);
    while (dt.hasNext()) {
        var rs, _ = (ResultArrayType)dt.getNext();
        int_arr = rs.INT_ARRAY;
        long_arr = rs.LONG_ARRAY;
        double_arr = rs.DOUBLE_ARRAY;
        boolean_arr = rs.BOOLEAN_ARRAY;
        string_arr = rs.STRING_ARRAY;
        float_arr = rs.FLOAT_ARRAY;
    }
    testDB.close();
    return;
}

function testDateTimeOutParams (int time, int date, int timestamp) (int count) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:DB.HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_SQL_CONNECTOR", "SA", "", {maximumPoolSize:1});
    }

    sql:Parameter para1 = {sqlType:sql:Type.INTEGER, value:10};
    sql:Parameter para2 = {sqlType:sql:Type.DATE, value:date};
    sql:Parameter para3 = {sqlType:sql:Type.TIME, value:time};
    sql:Parameter para4 = {sqlType:sql:Type.TIMESTAMP, value:timestamp};
    sql:Parameter para5 = {sqlType:sql:Type.DATETIME, value:timestamp};

    sql:Parameter para6 = {sqlType:sql:Type.DATE, direction:sql:Direction.OUT};
    sql:Parameter para7 = {sqlType:sql:Type.TIME, direction:sql:Direction.OUT};
    sql:Parameter para8 = {sqlType:sql:Type.TIMESTAMP, direction:sql:Direction.OUT};
    sql:Parameter para9 = {sqlType:sql:Type.DATETIME, direction:sql:Direction.OUT};

    sql:Parameter[] parameters = [para1, para2, para3, para4, para5, para6, para7, para8, para9];

    _ = testDB.call("{call TestDateTimeOutParams(?,?,?,?,?,?,?,?,?)}", parameters, null);

    table dt = testDB.select("SELECT count(*) as countval from DateTimeTypes where row_id = 10", null,
                                 typeof ResultCount);
    while (dt.hasNext()) {
        var rs, _ = (ResultCount)dt.getNext();
        count = rs.COUNTVAL;
    }
    testDB.close();
    return;
}

function testStructOutParameters () (any) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:DB.HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_SQL_CONNECTOR", "SA", "", {maximumPoolSize:1});
    }
    sql:Parameter para1 = {sqlType:sql:Type.STRUCT, direction:sql:Direction.OUT};
    sql:Parameter[] parameters = [para1];
    _ = testDB.call("{call TestStructOut(?)}", parameters, null);
    testDB.close();
    return para1.value;
}

function testComplexTypeRetrieval () (string s1, string s2, string s3, string s4) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:DB.HSQLDB_FILE, "./target/tempdb/",
                                   0, "TEST_SQL_CONNECTOR", "SA", "", {maximumPoolSize:1});
    }
    table dt = testDB.select("SELECT * from DataTypeTable where row_id = 1", null, null);
    var x, _ = <xml>dt;
    s1 = <string>x;

    dt = testDB.select("SELECT * from DateTimeTypes where row_id = 1", null, null);
    x, _ = <xml>dt;
    s2 = <string>x;

    dt = testDB.select("SELECT * from DataTypeTable where row_id = 1", null, null);
    var j, _ = <json>dt;
    s3 = j.toString();

    dt = testDB.select("SELECT * from DateTimeTypes where row_id = 1", null, null);
    j, _ = <json>dt;
    s4 = j.toString();

    testDB.close();
    return;
}

function testCloseConnectionPool () (int count) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:DB.HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_SQL_CONNECTOR", "SA", "", {maximumPoolSize:1});
    }
    table dt = testDB.select("SELECT COUNT(*) as countVal FROM INFORMATION_SCHEMA.SYSTEM_SESSIONS", null,
                                 typeof ResultCount);
    while (dt.hasNext()) {
        var rs, err = (ResultCount)dt.getNext();
        count = rs.COUNTVAL;
    }
    testDB.close();
    return;
}