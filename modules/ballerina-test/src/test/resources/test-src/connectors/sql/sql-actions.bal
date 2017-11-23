import ballerina.data.sql;

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

function testInsertTableData () (int) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:db.HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_SQL_CONNECTOR", "SA", "", {maximumPoolSize:1});
    }

    int insertCount = testDB.update ("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                                     values ('James', 'Clerk', 2, 5000.75, 'USA')", null);
    testDB.close ();
    return insertCount;
}

function testCreateTable () (int) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:db.HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_SQL_CONNECTOR", "SA", "", {maximumPoolSize:1});
    }

    int returnValue = testDB.update ("CREATE TABLE IF NOT EXISTS Students(studentID int, LastName varchar(255))",
                                     null);
    testDB.close ();
    return returnValue;
}

function testUpdateTableData () (int) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:db.HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_SQL_CONNECTOR", "SA", "", {maximumPoolSize:1});
    }

    sql:Parameter[] parameters = [];
    int updateCount = testDB.update ("Update Customers set country = 'UK' where registrationID = 1", parameters);
    testDB.close ();
    return updateCount;
}

function testGeneratedKeyOnInsert () (string) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:db.HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_SQL_CONNECTOR", "SA", "", {maximumPoolSize:1});
    }

    int insertCount;
    string[] generatedID;
    insertCount, generatedID = testDB.updateWithGeneratedKeys ("insert into Customers (firstName,lastName,
                             registrationID,creditLimit,country) values ('Mary', 'Williams', 3, 5000.75, 'USA')",
                                                               null, null);
    testDB.close ();
    return generatedID[0];
}

function testGeneratedKeyWithColumn () (string) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:db.HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_SQL_CONNECTOR", "SA", "", {maximumPoolSize:1});
    }

    int insertCount;
    string[] generatedID;
    string[] keyColumns;
    keyColumns = ["CUSTOMERID"];
    insertCount, generatedID = testDB.updateWithGeneratedKeys ("insert into Customers (firstName,lastName,
                               registrationID,creditLimit,country) values ('Kathy', 'Williams', 4, 5000.75, 'USA')",
                                                               null, keyColumns);
    testDB.close ();
    return generatedID[0];
}


function testInsertTableDataWithParameters () (int) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:db.HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_SQL_CONNECTOR", "SA", "", {maximumPoolSize:1});
    }

    sql:Parameter para1 = {sqlType:sql:data.VARCHAR, value:"Anne", direction:0};
    sql:Parameter para2 = {sqlType:sql:data.VARCHAR, value:"James", direction:0};
    sql:Parameter para3 = {sqlType:sql:data.INTEGER, value:3, direction:0};
    sql:Parameter para4 = {sqlType:sql:data.DOUBLE, value:5000.75, direction:0};
    sql:Parameter para5 = {sqlType:sql:data.VARCHAR, value:"UK", direction:0};
    sql:Parameter[] parameters = [para1, para2, para3, para4, para5];

    int insertCount = testDB.update ("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                                     values (?,?,?,?,?)", parameters);
    testDB.close ();
    return insertCount;
}

function testOutParameters () (any, any, any, any, any, any, any, any, any, any, any, any, any, any) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:db.HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_SQL_CONNECTOR", "SA", "", {maximumPoolSize:1});
    }

    sql:Parameter paraID = {sqlType:sql:data.INTEGER, value:"1", direction:0};
    sql:Parameter paraInt = {sqlType:sql:data.INTEGER, direction:1};
    sql:Parameter paraLong = {sqlType:sql:data.BIGINT, direction:1};
    sql:Parameter paraFloat = {sqlType:sql:data.FLOAT, direction:1};
    sql:Parameter paraDouble = {sqlType:sql:data.DOUBLE, direction:1};
    sql:Parameter paraBool = {sqlType:sql:data.BOOLEAN, direction:1};
    sql:Parameter paraString = {sqlType:sql:data.VARCHAR, direction:1};
    sql:Parameter paraNumeric = {sqlType:sql:data.NUMERIC, direction:1};
    sql:Parameter paraDecimal = {sqlType:sql:data.DECIMAL, direction:1};
    sql:Parameter paraReal = {sqlType:sql:data.REAL, direction:1};
    sql:Parameter paraTinyInt = {sqlType:sql:data.TINYINT, direction:1};
    sql:Parameter paraSmallInt = {sqlType:sql:data.SMALLINT, direction:1};
    sql:Parameter paraClob = {sqlType:sql:data.CLOB, direction:1};
    sql:Parameter paraBlob = {sqlType:sql:data.BLOB, direction:1};
    sql:Parameter paraBinary = {sqlType:sql:data.BINARY, direction:1};

    sql:Parameter[] parameters = [paraID, paraInt, paraLong, paraFloat, paraDouble, paraBool, paraString, paraNumeric,
                                  paraDecimal, paraReal, paraTinyInt, paraSmallInt, paraClob, paraBlob, paraBinary];
    _ = testDB.call ("{call TestOutParams(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}", parameters);
    testDB.close ();
    return paraInt.value, paraLong.value, paraFloat.value, paraDouble.value, paraBool.value, paraString.value,
           paraNumeric.value, paraDecimal.value, paraReal.value, paraTinyInt.value, paraSmallInt.value, paraClob.value,
           paraBlob.value, paraBinary.value;
}

function testNullOutParameters () (any, any, any, any, any, any, any, any, any, any, any, any, any, any) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:db.HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_SQL_CONNECTOR", "SA", "", {maximumPoolSize:1});
    }

    sql:Parameter paraID = {sqlType:sql:data.INTEGER, value:"2", direction:0};
    sql:Parameter paraInt = {sqlType:sql:data.INTEGER, direction:1};
    sql:Parameter paraLong = {sqlType:sql:data.BIGINT, direction:1};
    sql:Parameter paraFloat = {sqlType:sql:data.FLOAT, direction:1};
    sql:Parameter paraDouble = {sqlType:sql:data.DOUBLE, direction:1};
    sql:Parameter paraBool = {sqlType:sql:data.BOOLEAN, direction:1};
    sql:Parameter paraString = {sqlType:sql:data.VARCHAR, direction:1};
    sql:Parameter paraNumeric = {sqlType:sql:data.NUMERIC, direction:1};
    sql:Parameter paraDecimal = {sqlType:sql:data.DECIMAL, direction:1};
    sql:Parameter paraReal = {sqlType:sql:data.REAL, direction:1};
    sql:Parameter paraTinyInt = {sqlType:sql:data.TINYINT, direction:1};
    sql:Parameter paraSmallInt = {sqlType:sql:data.SMALLINT, direction:1};
    sql:Parameter paraClob = {sqlType:sql:data.CLOB, direction:1};
    sql:Parameter paraBlob = {sqlType:sql:data.BLOB, direction:1};
    sql:Parameter paraBinary = {sqlType:sql:data.BINARY, direction:1};

    sql:Parameter[] parameters = [paraID, paraInt, paraLong, paraFloat, paraDouble, paraBool, paraString, paraNumeric,
                                  paraDecimal, paraReal, paraTinyInt, paraSmallInt, paraClob, paraBlob, paraBinary];
    _ = testDB.call ("{call TestOutParams(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}", parameters);
    testDB.close ();
    return paraInt.value, paraLong.value, paraFloat.value, paraDouble.value, paraBool.value, paraString.value,
           paraNumeric.value, paraDecimal.value, paraReal.value, paraTinyInt.value, paraSmallInt.value, paraClob.value,
           paraBlob.value, paraBinary.value;
}

function testINParameters () (int) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:db.HSQLDB_FILE, "./target/tempdb/",
                                                0, "TEST_SQL_CONNECTOR", "SA", "", {maximumPoolSize:1});
    }

    sql:Parameter paraID = {sqlType:sql:data.INTEGER, value:3, direction:0};
    sql:Parameter paraInt = {sqlType:sql:data.INTEGER, value:1, direction:0};
    sql:Parameter paraLong = {sqlType:sql:data.BIGINT, value:"9223372036854774807", direction:0};
    sql:Parameter paraFloat = {sqlType:sql:data.FLOAT, value:123.34, direction:0};
    sql:Parameter paraDouble = {sqlType:sql:data.DOUBLE, value:2139095039, direction:0};
    sql:Parameter paraBool = {sqlType:sql:data.BOOLEAN, value:true, direction:0};
    sql:Parameter paraString = {sqlType:sql:data.VARCHAR, value:"Hello", direction:0};
    sql:Parameter paraNumeric = {sqlType:sql:data.NUMERIC, value:1234.567, direction:0};
    sql:Parameter paraDecimal = {sqlType:sql:data.DECIMAL, value:1234.567, direction:0};
    sql:Parameter paraReal = {sqlType:sql:data.REAL, value:1234.567, direction:0};
    sql:Parameter paraTinyInt = {sqlType:sql:data.TINYINT, value:1, direction:0};
    sql:Parameter paraSmallInt = {sqlType:sql:data.SMALLINT, value:5555, direction:0};
    sql:Parameter paraClob = {sqlType:sql:data.CLOB, value:"very long text", direction:0};
    sql:Parameter paraBlob = {sqlType:sql:data.BLOB, value:"YmxvYiBkYXRh", direction:0};
    sql:Parameter paraBinary = {sqlType:sql:data.BINARY, value:"d3NvMiBiYWxsZXJpbmEgYmluYXJ5IHRlc3Qu", direction:0};

    sql:Parameter[] parameters = [paraID, paraInt, paraLong, paraFloat, paraDouble, paraBool, paraString, paraNumeric,
                                  paraDecimal, paraReal, paraTinyInt, paraSmallInt, paraClob, paraBlob, paraBinary];
    int insertCount = testDB.update ("INSERT INTO DataTypeTable (row_id,int_type, long_type,
            float_type, double_type, boolean_type, string_type, numeric_type, decimal_type, real_type, tinyint_type,
            smallint_type, clob_type, blob_type, binary_type) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", parameters);
    testDB.close ();
    return insertCount;
}

function testNullINParameterValues () (int) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector( sql:db.HSQLDB_FILE, "./target/tempdb/",
                                        0, "TEST_SQL_CONNECTOR", "SA", "", { maximumPoolSize:1});
    }

    sql:Parameter paraID = {sqlType:sql:data.INTEGER, value:4, direction:0};
    sql:Parameter paraInt = {sqlType:sql:data.INTEGER, direction:0};
    sql:Parameter paraLong = {sqlType:sql:data.BIGINT, direction:0};
    sql:Parameter paraFloat = {sqlType:sql:data.FLOAT, direction:0};
    sql:Parameter paraDouble = {sqlType:sql:data.DOUBLE, direction:0};
    sql:Parameter paraBool = {sqlType:sql:data.BOOLEAN, direction:0};
    sql:Parameter paraString = {sqlType:sql:data.VARCHAR, direction:0};
    sql:Parameter paraNumeric = {sqlType:sql:data.NUMERIC, direction:0};
    sql:Parameter paraDecimal = {sqlType:sql:data.DECIMAL, direction:0};
    sql:Parameter paraReal = {sqlType:sql:data.REAL, direction:0};
    sql:Parameter paraTinyInt = {sqlType:sql:data.TINYINT, direction:0};
    sql:Parameter paraSmallInt = {sqlType:sql:data.SMALLINT, direction:0};
    sql:Parameter paraClob = {sqlType:sql:data.CLOB, direction:0};
    sql:Parameter paraBlob = {sqlType:sql:data.BLOB, direction:0};
    sql:Parameter paraBinary = {sqlType:sql:data.BINARY, direction:0};

    sql:Parameter[] parameters = [paraID, paraInt, paraLong, paraFloat, paraDouble, paraBool, paraString, paraNumeric,
                                  paraDecimal, paraReal, paraTinyInt, paraSmallInt, paraClob, paraBlob, paraBinary];
    int insertCount = testDB.update ("INSERT INTO DataTypeTable (row_id, int_type, long_type,
            float_type, double_type, boolean_type, string_type, numeric_type, decimal_type, real_type, tinyint_type,
            smallint_type, clob_type, blob_type, binary_type) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", parameters);
    testDB.close ();
    return insertCount;
}

function testNullINParameters () (int) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector( sql:db.HSQLDB_FILE, "./target/tempdb/",
                                                0, "TEST_SQL_CONNECTOR", "SA", "", { maximumPoolSize:1});
    }

    sql:Parameter paraID = {sqlType:sql:data.INTEGER, value:10, direction:0};

    sql:Parameter[] parameters = [paraID, null, null, null, null, null, null, null,
                                  null, null, null, null, null, null, null];
    int insertCount = testDB.update ("INSERT INTO DataTypeTable (row_id,int_type, long_type,
    float_type, double_type, boolean_type, string_type, numeric_type, decimal_type, real_type, tinyint_type,
    smallint_type, clob_type, blob_type, binary_type) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", parameters);
    testDB.close ();
    return insertCount;
}

function testINOutParameters () (any, any, any, any, any, any, any, any, any, any, any, any, any, any) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:db.HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_SQL_CONNECTOR", "SA", "", {maximumPoolSize:1});
    }

    sql:Parameter paraID = {sqlType:sql:data.INTEGER, value:5, direction:0};
    sql:Parameter paraInt = {sqlType:sql:data.INTEGER, value:10, direction:2};
    sql:Parameter paraLong = {sqlType:sql:data.BIGINT, value:"9223372036854774807", direction:2};
    sql:Parameter paraFloat = {sqlType:sql:data.FLOAT, value:123.34, direction:2};
    sql:Parameter paraDouble = {sqlType:sql:data.DOUBLE, value:2139095039, direction:2};
    sql:Parameter paraBool = {sqlType:sql:data.BOOLEAN, value:true, direction:2};
    sql:Parameter paraString = {sqlType:sql:data.VARCHAR, value:"Hello", direction:2};
    sql:Parameter paraNumeric = {sqlType:sql:data.NUMERIC, value:1234.567, direction:2};
    sql:Parameter paraDecimal = {sqlType:sql:data.DECIMAL, value:1234.567, direction:2};
    sql:Parameter paraReal = {sqlType:sql:data.REAL, value:1234.567, direction:2};
    sql:Parameter paraTinyInt = {sqlType:sql:data.TINYINT, value:1, direction:2};
    sql:Parameter paraSmallInt = {sqlType:sql:data.SMALLINT, value:5555, direction:2};
    sql:Parameter paraClob = {sqlType:sql:data.CLOB, value:"very long text", direction:2};
    sql:Parameter paraBlob = {sqlType:sql:data.BLOB, value:"YmxvYiBkYXRh", direction:2};
    sql:Parameter paraBinary = {sqlType:sql:data.BINARY, value:"d3NvMiBiYWxsZXJpbmEgYmluYXJ5IHRlc3Qu", direction:2};

    sql:Parameter[] parameters = [paraID, paraInt, paraLong, paraFloat, paraDouble, paraBool, paraString, paraNumeric,
                                  paraDecimal, paraReal, paraTinyInt, paraSmallInt, paraClob, paraBlob, paraBinary];
    _ = testDB.call ("{call TestINOUTParams(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}", parameters);
    testDB.close ();
    return paraInt.value, paraLong.value, paraFloat.value, paraDouble.value, paraBool.value, paraString.value,
           paraNumeric.value, paraDecimal.value, paraReal.value, paraTinyInt.value, paraSmallInt.value, paraClob.value,
           paraBlob.value, paraBinary.value;
}

function testNullINOutParameters ()
(any, any, any, any, any, any, any, any, any, any, any, any, any, any) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:db.HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_SQL_CONNECTOR", "SA", "", {maximumPoolSize:1});
    }

    sql:Parameter paraID = {sqlType:sql:data.INTEGER, value:"6", direction:0};
    sql:Parameter paraInt = {sqlType:sql:data.INTEGER, direction:2};
    sql:Parameter paraLong = {sqlType:sql:data.BIGINT, direction:2};
    sql:Parameter paraFloat = {sqlType:sql:data.FLOAT, direction:2};
    sql:Parameter paraDouble = {sqlType:sql:data.DOUBLE, direction:2};
    sql:Parameter paraBool = {sqlType:sql:data.BOOLEAN, direction:2};
    sql:Parameter paraString = {sqlType:sql:data.VARCHAR, direction:2};
    sql:Parameter paraNumeric = {sqlType:sql:data.NUMERIC, direction:2};
    sql:Parameter paraDecimal = {sqlType:sql:data.DECIMAL, direction:2};
    sql:Parameter paraReal = {sqlType:sql:data.REAL, direction:2};
    sql:Parameter paraTinyInt = {sqlType:sql:data.TINYINT, direction:2};
    sql:Parameter paraSmallInt = {sqlType:sql:data.SMALLINT, direction:2};
    sql:Parameter paraClob = {sqlType:sql:data.CLOB, direction:2};
    sql:Parameter paraBlob = {sqlType:sql:data.BLOB, direction:2};
    sql:Parameter paraBinary = {sqlType:sql:data.BINARY, direction:2};

    sql:Parameter[] parameters = [paraID, paraInt, paraLong, paraFloat, paraDouble, paraBool, paraString, paraNumeric,
                                  paraDecimal, paraReal, paraTinyInt, paraSmallInt, paraClob, paraBlob, paraBinary];
    _ = testDB.call ("{call TestINOUTParams(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}", parameters);
    testDB.close ();
    return paraInt.value, paraLong.value, paraFloat.value, paraDouble.value, paraBool.value, paraString.value,
           paraNumeric.value, paraDecimal.value, paraReal.value, paraTinyInt.value, paraSmallInt.value, paraClob.value,
           paraBlob.value, paraBinary.value;
}

function testEmptySQLType () (int) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:db.HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_SQL_CONNECTOR", "SA", "", {maximumPoolSize:1});
    }

    sql:Parameter para1 = {value:"Anne", direction:0};
    sql:Parameter[] parameters = [para1];
    int insertCount = testDB.update ("Insert into Customers (firstName) values (?)", parameters);
    testDB.close ();
    return insertCount;
}

function testArrayOutParameters () (any, any, any, any, any, any) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:db.HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_SQL_CONNECTOR", "SA", "", {maximumPoolSize:1});
    }

    string firstName;
    sql:Parameter para1 = {sqlType:sql:data.ARRAY, direction:1};
    sql:Parameter para2 = {sqlType:sql:data.ARRAY, direction:1};
    sql:Parameter para3 = {sqlType:sql:data.ARRAY, direction:1};
    sql:Parameter para4 = {sqlType:sql:data.ARRAY, direction:1};
    sql:Parameter para5 = {sqlType:sql:data.ARRAY, direction:1};
    sql:Parameter para6 = {sqlType:sql:data.ARRAY, direction:1};
    sql:Parameter[] parameters = [para1, para2, para3, para4, para5, para6];
    _ = testDB.call ("{call TestArrayOutParams(?,?,?,?,?,?)}", parameters);
    testDB.close ();
    return para1.value, para2.value, para3.value, para4.value, para5.value, para6.value;
}

function testArrayInOutParameters () (any, any, any, any, any, any, any) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:db.HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_SQL_CONNECTOR", "SA", "", {maximumPoolSize:1});
    }

    sql:Parameter para1 = {sqlType:sql:data.INTEGER, value:3, direction:0};
    sql:Parameter para2 = {sqlType:sql:data.INTEGER, direction:1};
    sql:Parameter para3 = {sqlType:sql:data.ARRAY, value:"10,20,30", direction:2};
    sql:Parameter para4 = {sqlType:sql:data.ARRAY, value:"10000000, 20000000, 30000000", direction:2};
    sql:Parameter para5 = {sqlType:sql:data.ARRAY, value:"2454.23, 55594.49, 87964.123", direction:2};
    sql:Parameter para6 = {sqlType:sql:data.ARRAY, value:"2454.23, 55594.49, 87964.123", direction:2};
    sql:Parameter para7 = {sqlType:sql:data.ARRAY, value:"FALSE, FALSE, TRUE", direction:2};
    sql:Parameter para8 = {sqlType:sql:data.ARRAY, value:"Hello,Ballerina,Lang", direction:2};
    sql:Parameter[] parameters = [para1, para2, para3, para4, para5, para6, para7, para8];

    _ = testDB.call ("{call TestArrayInOutParams(?,?,?,?,?,?,?,?)}", parameters);
    testDB.close ();
    return para2.value, para3.value, para4.value, para5.value, para6.value, para7.value, para8.value;
}

function testBatchUpdate () (int[]) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:db.HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_SQL_CONNECTOR", "SA", "", {maximumPoolSize:1});
    }

    //Batch 1
    sql:Parameter para1 = {sqlType:sql:data.VARCHAR, value:"Alex", direction:0};
    sql:Parameter para2 = {sqlType:sql:data.VARCHAR, value:"Smith", direction:0};
    sql:Parameter para3 = {sqlType:sql:data.INTEGER, value:20, direction:0};
    sql:Parameter para4 = {sqlType:sql:data.DOUBLE, value:3400.5, direction:0};
    sql:Parameter para5 = {sqlType:sql:data.VARCHAR, value:"Colombo", direction:0};
    sql:Parameter[] parameters1 = [para1, para2, para3, para4, para5];

    //Batch 2
    para1 = {sqlType:sql:data.VARCHAR, value:"Alex", direction:0};
    para2 = {sqlType:sql:data.VARCHAR, value:"Smith", direction:0};
    para3 = {sqlType:sql:data.INTEGER, value:20, direction:0};
    para4 = {sqlType:sql:data.DOUBLE, value:3400.5, direction:0};
    para5 = {sqlType:sql:data.VARCHAR, value:"Colombo", direction:0};
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
        create sql:ClientConnector(sql:db.HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_SQL_CONNECTOR", "SA", "", {maximumPoolSize:1});
    }

    //Batch 1
    sql:Parameter para0 = {sqlType:sql:data.INTEGER, value:111, direction:0};
    sql:Parameter para1 = {sqlType:sql:data.VARCHAR, value:"Alex", direction:0};
    sql:Parameter para2 = {sqlType:sql:data.VARCHAR, value:"Smith", direction:0};
    sql:Parameter para3 = {sqlType:sql:data.INTEGER, value:20, direction:0};
    sql:Parameter para4 = {sqlType:sql:data.DOUBLE, value:3400.5, direction:0};
    sql:Parameter para5 = {sqlType:sql:data.VARCHAR, value:"Colombo", direction:0};
    sql:Parameter[] parameters1 = [para0, para1, para2, para3, para4, para5];

    //Batch 2
    para0 = {sqlType:sql:data.INTEGER, value:222, direction:0};
    para1 = {sqlType:sql:data.VARCHAR, value:"Alex", direction:0};
    para2 = {sqlType:sql:data.VARCHAR, value:"Smith", direction:0};
    para3 = {sqlType:sql:data.INTEGER, value:20, direction:0};
    para4 = {sqlType:sql:data.DOUBLE, value:3400.5, direction:0};
    para5 = {sqlType:sql:data.VARCHAR, value:"Colombo", direction:0};
    sql:Parameter[] parameters2 = [para0, para1, para2, para3, para4, para5];


    //Batch 3
    para0 = {sqlType:sql:data.INTEGER, value:222, direction:0};
    para1 = {sqlType:sql:data.VARCHAR, value:"Alex", direction:0};
    para2 = {sqlType:sql:data.VARCHAR, value:"Smith", direction:0};
    para3 = {sqlType:sql:data.INTEGER, value:20, direction:0};
    para4 = {sqlType:sql:data.DOUBLE, value:3400.5, direction:0};
    para5 = {sqlType:sql:data.VARCHAR, value:"Colombo", direction:0};
    sql:Parameter[] parameters3 = [para0, para1, para2, para3, para4, para5];

    //Batch 4
    para0 = {sqlType:sql:data.INTEGER, value:333, direction:0};
    para1 = {sqlType:sql:data.VARCHAR, value:"Alex", direction:0};
    para2 = {sqlType:sql:data.VARCHAR, value:"Smith", direction:0};
    para3 = {sqlType:sql:data.INTEGER, value:20, direction:0};
    para4 = {sqlType:sql:data.DOUBLE, value:3400.5, direction:0};
    para5 = {sqlType:sql:data.VARCHAR, value:"Colombo", direction:0};
    sql:Parameter[] parameters4 = [para0, para1, para2, para3, para4, para5];

    sql:Parameter[][] parameters = [parameters1, parameters2, parameters3,parameters4];

    updateCount = testDB.batchUpdate("Insert into Customers (customerId, firstName,lastName,registrationID,creditLimit,
        country) values (?,?,?,?,?,?)", parameters);


    sql:Parameter[] params = [];
    datatable dt = testDB.select ("SELECT count(*) as countval from Customers where customerId in (111,222,333)",
                                  params);
    ResultCount rs;
    while (dt.hasNext()) {
        any dataStruct = dt.getNext();
        rs, _ = (ResultCount) dataStruct;
        count = rs.COUNTVAL;
    }

    testDB.close();
    return updateCount, count;
}

function testBatchUpdateWithNullParam () (int[]) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:db.HSQLDB_FILE, "./target/tempdb/",
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
        create sql:ClientConnector(sql:db.HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_SQL_CONNECTOR", "SA", "", {maximumPoolSize:1});
    }
    string stmt = "Insert into DateTimeTypes(row_id,date_type,time_type,datetime_type,timestamp_type) values (?,?,?,?,?)";
    int[] returnValues = [];
    sql:Parameter para1 = {sqlType:sql:data.INTEGER, value:1, direction:0};
    sql:Parameter para2 = {sqlType:sql:data.DATE, value:"2017-01-30-08:01", direction:0};
    sql:Parameter para3 = {sqlType:sql:data.TIME, value:"13:27:01.999999+08:33", direction:0};
    sql:Parameter para4 = {sqlType:sql:data.TIMESTAMP, value:"2017-01-30T13:27:01.999-08:00", direction:0};
    sql:Parameter para5 = {sqlType:sql:data.DATETIME, value:"2017-01-30T13:27:01.999999Z", direction:0};
    sql:Parameter[] parameters = [para1, para2, para3, para4, para5];

    int insertCount1 = testDB.update(stmt, parameters);
    returnValues[0] = insertCount1;

    para1 = {sqlType:sql:data.INTEGER, value:2, direction:0};
    para2 = {sqlType:sql:data.DATE, value:"-2017-01-30Z", direction:0};
    para3 = {sqlType:sql:data.TIME, value:"13:27:01+08:33", direction:0};
    para4 = {sqlType:sql:data.TIMESTAMP, value:"2017-01-30T13:27:01.999", direction:0};
    para5 = {sqlType:sql:data.DATETIME, value:"-2017-01-30T13:27:01.999999-08:30", direction:0};
    parameters = [para1, para2, para3, para4, para5];

    int insertCount2 = testDB.update(stmt, parameters);
    returnValues[1] = insertCount2;


    Time timeNow = currentTime();
    para1 = {sqlType:sql:data.INTEGER, value:3};
    para2 = {sqlType:sql:data.DATE, value:timeNow};
    para3 = {sqlType:sql:data.TIME, value:timeNow};
    para4 = {sqlType:sql:data.TIMESTAMP, value:timeNow};
    para5 = {sqlType:sql:data.DATETIME, value:timeNow};
    parameters = [para1, para2, para3, para4, para5];

    int insertCount3 = testDB.update(stmt, parameters);
    returnValues[2] = insertCount3;

    testDB.close();
    return returnValues;
}

function testSelectIntFloatData () (int int_type, int long_type, float float_type, float double_type) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:db.HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_SQL_CONNECTOR", "SA", "", {maximumPoolSize:1});
    }

    sql:Parameter[] parameters = [];
    TypeCastError err;
    ResultDataType rs;
    datatable dt = testDB.select ("SELECT  int_type, long_type, float_type, double_type from DataTypeTable
                                   where row_id = 1", parameters);
    while (dt.hasNext()) {
        any dataStruct = dt.getNext();
        rs, err = (ResultDataType) dataStruct;
        int_type = rs.INT_TYPE;
        long_type = rs.LONG_TYPE;
        float_type = rs.FLOAT_TYPE;
        double_type = rs.DOUBLE_TYPE;
    }
    testDB.close ();
    return;
}

function testSelectData () (string firstName) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:db.HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_SQL_CONNECTOR", "SA", "", {maximumPoolSize:1});
    }

    datatable dt = testDB.select ("SELECT  FirstName from Customers where registrationID = 1", null);
    TypeCastError err;
    ResultCustomers rs;
    while (dt.hasNext()) {
        any dataStruct = dt.getNext();
        rs, err = (ResultCustomers) dataStruct;
        firstName = rs.FIRSTNAME;
    }
    testDB.close ();
    return;
}

function testCallProcedure () (string firstName) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:db.HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_SQL_CONNECTOR", "SA", "", {maximumPoolSize:1});
    }

    _ = testDB.call ("{call InsertPersonData(100,'James')}", null);
    datatable dt = testDB.select ("SELECT  FirstName from Customers where registrationID = 100", null);
    TypeCastError err;
    ResultCustomers rs;
    while (dt.hasNext()) {
        any dataStruct = dt.getNext();
        rs, err = (ResultCustomers) dataStruct;
        firstName = rs.FIRSTNAME;
    }
    testDB.close ();
    return;
}

function testCallProcedureWithResultSet () (string firstName) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:db.HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_SQL_CONNECTOR", "SA", "", {maximumPoolSize:1});
    }

    sql:Parameter[] parameters = [];
    datatable dt = testDB.call ("{call SelectPersonData()}", parameters);
    TypeCastError err;
    ResultCustomers rs;
    while (dt.hasNext()) {
        any dataStruct = dt.getNext();
        rs, err = (ResultCustomers) dataStruct;
        firstName = rs.FIRSTNAME;
    }
    testDB.close ();
    return;
}

function testQueryParameters () (string firstName) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:db.HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_SQL_CONNECTOR", "SA", "", {maximumPoolSize:1});
    }

    sql:Parameter para1 = {sqlType:sql:data.INTEGER, value:1, direction:0};
    sql:Parameter[] parameters = [para1];
    datatable dt = testDB.select ("SELECT  FirstName from Customers where registrationID = ?", parameters);
    TypeCastError err;
    ResultCustomers rs;
    while (dt.hasNext()) {
        any dataStruct = dt.getNext();
        rs, err = (ResultCustomers) dataStruct;
        firstName = rs.FIRSTNAME;
    }
    testDB.close ();
    return;
}

function testArrayofQueryParameters () (string firstName) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:db.HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_SQL_CONNECTOR", "SA", "", {maximumPoolSize:1});
    }

    int[] intDataArray = [1,4343];
    string[] stringDataArray = ["A", "B"];
    float[] doubleArray = [233.4, 433.4];
    sql:Parameter para0 = {sqlType:sql:data.VARCHAR, value:"Johhhn", direction:0};
    sql:Parameter para1 = {sqlType:sql:data.INTEGER, value:intDataArray, direction:0};
    sql:Parameter para2 = {sqlType:sql:data.VARCHAR, value:stringDataArray, direction:0};
    sql:Parameter para3 = {sqlType:sql:data.DOUBLE, value:doubleArray, direction:0};
    sql:Parameter[] parameters = [para0,para1, para2, para3];
    datatable dt = testDB.select ("SELECT  FirstName from Customers where FirstName = ? or lastName = 'A' or
                    lastName = '\"BB\"' or registrationID in(?) or lastName in(?) or creditLimit in(?)", parameters);
    TypeCastError err;
    ResultCustomers rs;
    while (dt.hasNext()) {
        any dataStruct = dt.getNext();
        rs, err = (ResultCustomers) dataStruct;
        firstName = rs.FIRSTNAME;
    }
    testDB.close ();
    return;
}

function testBoolArrayofQueryParameters () (int value ) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:db.HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_SQL_CONNECTOR", "SA", "", {maximumPoolSize:1});
    }
    boolean accepted1 = false;
    boolean accepted2 = false;
    boolean accepted3 = true;
    boolean[] boolDataArray = [accepted1,accepted2,accepted3];


    datatable dt1 = testDB.select("SELECT blob_type from DataTypeTable where row_id = 1", null);
    blob blobData;
    while (dt1.hasNext()) {
        any dataStruct = dt1.getNext();
        var rs, err = (ResultBlob)dataStruct;
        blobData = rs.BLOB_TYPE;
    }
    blob[] blobDataArray = [blobData];

    sql:Parameter para0 = {sqlType:sql:data.INTEGER, value:1, direction:0};
    sql:Parameter para1 = {sqlType:sql:data.BOOLEAN, value:boolDataArray, direction:0};
    sql:Parameter para2 = {sqlType:sql:data.BLOB, value:blobDataArray, direction:0};
    sql:Parameter[] parameters = [para0,para1,para2];
    datatable dt = testDB.select ("SELECT  int_type from DataTypeTable where row_id = ? and boolean_type in(?) and
                                                            blob_type in (?)",
                                  parameters);
    TypeCastError err;
    ResultIntType rs;
    while (dt.hasNext()) {
        any dataStruct = dt.getNext();
        rs, err = (ResultIntType) dataStruct;
        value = rs.INT_TYPE;
    }
    testDB.close ();
    return;
}


function testArrayInParameters () (int insertCount, map int_arr, map long_arr, map double_arr, map string_arr,
                                   map boolean_arr, map float_arr) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:db.HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_SQL_CONNECTOR", "SA", "", {maximumPoolSize:1});
    }

    int[] intArray = [1];
    int[] longArray = [1503383034226, 1503383034224, 1503383034225];
    float[] floatArray = [245.23, 5559.49, 8796.123];
    float[] doubleArray = [1503383034226.23,  1503383034224.43, 1503383034225.123];
    boolean[] boolArray = [true, false, true];
    string[] stringArray = ["Hello","Ballerina"];
    sql:Parameter para1 = {sqlType:sql:data.INTEGER, value:2, direction:0};
    sql:Parameter para2 = {sqlType:sql:data.ARRAY, value:intArray, direction:0};
    sql:Parameter para3 = {sqlType:sql:data.ARRAY, value:longArray, direction:0};
    sql:Parameter para4 = {sqlType:sql:data.ARRAY, value:floatArray, direction:0};
    sql:Parameter para5 = {sqlType:sql:data.ARRAY, value:doubleArray, direction:0};
    sql:Parameter para6 = {sqlType:sql:data.ARRAY, value:boolArray, direction:0};
    sql:Parameter para7 = {sqlType:sql:data.ARRAY, value:stringArray, direction:0};
    sql:Parameter[] parameters = [para1, para2, para3, para4, para5, para6, para7];

    insertCount = testDB.update ("INSERT INTO ArrayTypes (row_id, int_array, long_array,
        float_array, double_array, boolean_array, string_array) values (?,?,?,?,?,?,?)", parameters);

    sql:Parameter[] params = [];
    datatable dt = testDB.select ("SELECT int_array, long_array, double_array, boolean_array,
        string_array, float_array from ArrayTypes where row_id = 2", params);
    ResultArrayType rs;
    while (dt.hasNext()) {
        any dataStruct = dt.getNext();
        rs, _ = (ResultArrayType) dataStruct;
        int_arr = rs.INT_ARRAY;
        long_arr = rs.LONG_ARRAY;
        double_arr = rs.DOUBLE_ARRAY;
        boolean_arr = rs.BOOLEAN_ARRAY;
        string_arr = rs.STRING_ARRAY;
        float_arr = rs.FLOAT_ARRAY;
    }
    testDB.close ();
    return;
}

function testDateTimeOutParams (int time, int date, int timestamp) (int count) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:db.HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_SQL_CONNECTOR", "SA", "", {maximumPoolSize:1});
    }

    sql:Parameter para1 = {sqlType:sql:data.INTEGER, value:10, direction:0};
    sql:Parameter para2 = {sqlType:sql:data.DATE, value:date, direction:0};
    sql:Parameter para3 = {sqlType:sql:data.TIME, value:time, direction:0};
    sql:Parameter para4 = {sqlType:sql:data.TIMESTAMP, value:timestamp, direction:0};
    sql:Parameter para5 = {sqlType:sql:data.DATETIME, value:timestamp, direction:0};

    sql:Parameter para6 = {sqlType:sql:data.DATE, direction:1};
    sql:Parameter para7 = {sqlType:sql:data.TIME, direction:1};
    sql:Parameter para8 = {sqlType:sql:data.TIMESTAMP, direction:1};
    sql:Parameter para9 = {sqlType:sql:data.DATETIME, direction:1};

    sql:Parameter[] parameters = [para1, para2, para3, para4, para5, para6, para7, para8, para9];

    _ = testDB.call("{call TestDateTimeOutParams(?,?,?,?,?,?,?,?,?)}", parameters);

    sql:Parameter[] emptyParam = [];
    datatable dt = testDB.select("SELECT count(*) as countval from DateTimeTypes where row_id = 10", emptyParam);
    ResultCount rs;
    while (dt.hasNext()) {
        any dataStruct = dt.getNext();
        rs, _ = (ResultCount)dataStruct;
        count = rs.COUNTVAL;
    }
    testDB.close();
    return;
}

function testStructOutParameters() (any) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:db.HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_SQL_CONNECTOR", "SA", "", {maximumPoolSize:1});
    }
    sql:Parameter para1 = {sqlType:sql:data.STRUCT, direction:1};
    sql:Parameter[] parameters = [para1];
    _ = testDB.call("{call TestStructOut(?)}", parameters);
    testDB.close();
    return para1.value;
}

function testCloseConnectionPool () (int count) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:db.HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_SQL_CONNECTOR", "SA", "", {maximumPoolSize:1});
    }
    sql:Parameter[] parameters = [];
    datatable dt = testDB.select ("SELECT COUNT(*) as countVal FROM INFORMATION_SCHEMA.SYSTEM_SESSIONS", parameters);
    TypeCastError err;
    ResultCount rs;
    while (dt.hasNext()) {
        any dataStruct = dt.getNext();
        rs, err = (ResultCount) dataStruct;
        count = rs.COUNTVAL;
    }
    testDB.close();
    return;
}