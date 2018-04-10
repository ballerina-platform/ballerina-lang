import ballerina/sql;
import ballerina/time;
import ballerina/io;

type ResultCustomers {
    string FIRSTNAME,
};

type ResultIntType {
    int INT_TYPE,
};

type ResultBlob {
    blob BLOB_TYPE,
};

type ResultDataType {
    int INT_TYPE,
    int LONG_TYPE,
    float FLOAT_TYPE,
    float DOUBLE_TYPE,
};

type ResultCount {
    int COUNTVAL,
};

type ResultArrayType {
    map INT_ARRAY,
    map LONG_ARRAY,
    map DOUBLE_ARRAY,
    map BOOLEAN_ARRAY,
    map STRING_ARRAY,
    map FLOAT_ARRAY,
};

type ResultDates {
    string DATE_TYPE,
    string TIME_TYPE,
    string TIMESTAMP_TYPE,
    string DATETIME_TYPE,
};

function testInsertTableData () returns (int) {
    endpoint sql:Client testDB {
        url: "hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username: "SA",
        options: {maximumPoolSize:1}
    };

    var insertCountRet = testDB -> update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                                         values ('James', 'Clerk', 2, 5000.75, 'USA')", ());
    int insertCount = check insertCountRet;
    _ = testDB -> close();
    return insertCount;
}

function testCreateTable () returns (int) {
    endpoint sql:Client testDB {
        url: "hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username: "SA",
        options: {maximumPoolSize:1}
    };

    var returnValueRet = testDB -> update("CREATE TABLE IF NOT EXISTS Students(studentID int, LastName varchar(255))",
        ());
    int returnValue = check returnValueRet;
    _ = testDB -> close();
    return returnValue;
}

function testUpdateTableData () returns (int) {
    endpoint sql:Client testDB {
        url: "hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username: "SA",
        options: {maximumPoolSize:1}
    };

    sql:Parameter[] parameters = [];
    var updateCountRet  = testDB -> update("Update Customers set country = 'UK' where registrationID = 1", parameters);
    _ = testDB -> close();
    int updateCount = check updateCountRet;
    return updateCount;
}

function testGeneratedKeyOnInsert () returns (string) {
    endpoint sql:Client testDB {
        url: "hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username: "SA",
        options: {maximumPoolSize:1}
    };

    string returnVal;

    var x = testDB -> updateWithGeneratedKeys("insert into Customers (firstName,lastName,
            registrationID,creditLimit,country) values ('Mary', 'Williams', 3, 5000.75, 'USA')", (), ());

    match x {
        (int, string[]) y  =>{
            int a;
            string[] b;
            (a, b) = y;
            returnVal = b[0];
        }
        error err1 =>{
            returnVal = err1.message;
        }
    }

    _ = testDB -> close();
    return returnVal;
}

function testGeneratedKeyWithColumn () returns (string) {
    endpoint sql:Client testDB {
        url: "hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username: "SA",
        options: {maximumPoolSize:1}
    };

    int insertCount;
    string[] generatedID;
    string[] keyColumns = ["CUSTOMERID"];
    string returnVal;
    var x = testDB -> updateWithGeneratedKeys("insert into Customers (firstName,lastName,
                               registrationID,creditLimit,country) values ('Kathy', 'Williams', 4, 5000.75, 'USA')",
                                (), keyColumns);
    match x {
        (int, string[]) y  =>{
            int a;
            string[] b;
            (a, b) = y;
            returnVal = b[0];
        }
        error err1 =>{
            returnVal = err1.message;
        }
    }

    _ = testDB -> close();
    return returnVal;
}

function testSelectData () returns (string) {
    endpoint sql:Client testDB {
        url: "hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username: "SA",
        options: {maximumPoolSize:1}
    };

    var dtRet = testDB -> select("SELECT  FirstName from Customers where registrationID = 1", (),
                             typeof ResultCustomers);
    table dt = check dtRet;
    string firstName;

    while (dt.hasNext()) {
        ResultCustomers rs = check <ResultCustomers>dt.getNext();
        firstName = rs.FIRSTNAME;
    }
    _ = testDB -> close();
    return firstName;
}

function testSelectIntFloatData () returns (int, int, float, float) {
    endpoint sql:Client testDB {
        url: "hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username: "SA",
        options: {maximumPoolSize:1}
    };

    var dtRet = testDB -> select("SELECT  int_type, long_type, float_type, double_type from DataTypeTable
        where row_id = 1", (), typeof ResultDataType);
    table dt = check dtRet;
    int int_type;
    int long_type;
    float float_type;
    float double_type;
    while (dt.hasNext()) {
        ResultDataType rs = check <ResultDataType>dt.getNext();
        int_type = rs.INT_TYPE;
        long_type = rs.LONG_TYPE;
        float_type = rs.FLOAT_TYPE;
        double_type = rs.DOUBLE_TYPE;
    }
    _ = testDB -> close();
    return (int_type, long_type, float_type, double_type);
}

function testCallProcedure () returns (string) {
    endpoint sql:Client testDB {
        url: "hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username: "SA",
        options: {maximumPoolSize:1}
    };

    _ = testDB -> call("{call InsertPersonData(100,'James')}", (), ());
    var dtRet = testDB -> select("SELECT  FirstName from Customers where registrationID = 100", (),
                             typeof ResultCustomers);
    table dt = check dtRet;
    string firstName;
    while (dt.hasNext()) {
        ResultCustomers rs = check <ResultCustomers>dt.getNext();
        firstName = rs.FIRSTNAME;
    }
    _ = testDB -> close();
    return firstName;
}

function testCallProcedureWithResultSet () returns (string) {
    endpoint sql:Client testDB {
        url: "hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username: "SA",
        options: {maximumPoolSize:1}
    };

    var dtsRet = testDB -> call("{call SelectPersonData()}", (), typeof ResultCustomers);
    table[] dts = check dtsRet;

    string firstName;
    while (dts[0].hasNext()) {
        ResultCustomers rs = check <ResultCustomers>dts[0].getNext();
        firstName = rs.FIRSTNAME;
    }
    _ = testDB -> close();
    return firstName;
}

function testCallProcedureWithMultipleResultSets () returns (string, string) {
    endpoint sql:Client testDB {
        url: "hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username: "SA",
        options: {maximumPoolSize:1}
    };

    var dtsRet = testDB -> call("{call SelectPersonDataMultiple()}", (), typeof ResultCustomers);
    table[] dts = check dtsRet;

    string firstName1;
    string firstName2;

    while (dts[0].hasNext()) {
        ResultCustomers rs = check <ResultCustomers>dts[0].getNext();
        firstName1 = rs.FIRSTNAME;
    }

    while (dts[1].hasNext()) {
        ResultCustomers rs = check <ResultCustomers>dts[1].getNext();
        firstName2 = rs.FIRSTNAME;
    }

    _ = testDB -> close();
    return (firstName1, firstName2);
}

function testQueryParameters () returns (string) {
    endpoint sql:Client testDB {
        url: "hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username: "SA",
        options: {maximumPoolSize:1}
    };

    sql:Parameter para1 = {sqlType:sql:TYPE_INTEGER, value:1};
    sql:Parameter[] parameters = [para1];
    var dtRet =  testDB -> select("SELECT  FirstName from Customers where registrationID = ?", parameters,
        typeof ResultCustomers);
    table dt = check dtRet;

    string firstName;

    while (dt.hasNext()) {
        ResultCustomers rs = check <ResultCustomers>dt.getNext();
        firstName = rs.FIRSTNAME;
    }
    _ = testDB -> close();
    return firstName;
}

function testInsertTableDataWithParameters () returns (int) {
    endpoint sql:Client testDB {
        url: "hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username: "SA",
        options: {maximumPoolSize:1}
    };

    sql:Parameter para1 = {sqlType:sql:TYPE_VARCHAR, value:"Anne", direction:sql:DIRECTION_IN};
    sql:Parameter para2 = {sqlType:sql:TYPE_VARCHAR, value:"James", direction:sql:DIRECTION_IN};
    sql:Parameter para3 = {sqlType:sql:TYPE_INTEGER, value:3, direction:sql:DIRECTION_IN};
    sql:Parameter para4 = {sqlType:sql:TYPE_DOUBLE, value:5000.75, direction:sql:DIRECTION_IN};
    sql:Parameter para5 = {sqlType:sql:TYPE_VARCHAR, value:"UK", direction:sql:DIRECTION_IN};
    sql:Parameter[] parameters = [para1, para2, para3, para4, para5];

    var insertCountRet = testDB -> update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                                     values (?,?,?,?,?)", parameters);
    int insertCount = check insertCountRet;

    _ = testDB -> close();
    return insertCount;
}

function testArrayofQueryParameters () returns (string) {
    endpoint sql:Client testDB {
        url: "hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username: "SA",
        options: {maximumPoolSize:1}
    };

    int[] intDataArray = [1, 4343];
    string[] stringDataArray = ["A", "B"];
    float[] doubleArray = [233.4, 433.4];
    sql:Parameter para0 = {sqlType:sql:TYPE_VARCHAR, value:"Johhhn"};
    sql:Parameter para1 = {sqlType:sql:TYPE_INTEGER, value:intDataArray};
    sql:Parameter para2 = {sqlType:sql:TYPE_VARCHAR, value:stringDataArray};
    sql:Parameter para3 = {sqlType:sql:TYPE_DOUBLE, value:doubleArray};
    sql:Parameter[] parameters = [para0, para1, para2, para3];

    var dtRet = testDB -> select("SELECT  FirstName from Customers where FirstName = ? or lastName = 'A' or
        lastName = '\"BB\"' or registrationID in(?) or lastName in(?) or creditLimit in(?)", parameters,
        typeof ResultCustomers);
    table dt = check dtRet;

    string firstName;
    while (dt.hasNext()) {
        ResultCustomers rs = check <ResultCustomers>dt.getNext();
        firstName = rs.FIRSTNAME;
    }
    _ = testDB -> close();
    return firstName;
}

function testBoolArrayofQueryParameters () returns (int) {
    endpoint sql:Client testDB {
        url: "hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username: "SA",
        options: {maximumPoolSize:1}
    };

    boolean accepted1 = false;
    boolean accepted2 = false;
    boolean accepted3 = true;
    boolean[] boolDataArray = [accepted1, accepted2, accepted3];

    var dt1Ret = testDB -> select("SELECT blob_type from DataTypeTable where row_id = 1", (), typeof ResultBlob);
    table dt1 = check dt1Ret;

    blob blobData;
    while (dt1.hasNext()) {
        ResultBlob rs = check <ResultBlob>dt1.getNext();
        blobData = rs.BLOB_TYPE;
    }
    blob[] blobDataArray = [blobData];

    sql:Parameter para0 = {sqlType:sql:TYPE_INTEGER, value:1};
    sql:Parameter para1 = {sqlType:sql:TYPE_BOOLEAN, value:boolDataArray};
    sql:Parameter para2 = {sqlType:sql:TYPE_BLOB, value:blobDataArray};
    sql:Parameter[] parameters = [para0, para1, para2];

    var dtRet = testDB -> select("SELECT  int_type from DataTypeTable where row_id = ? and boolean_type in(?) and
        blob_type in (?)", parameters, typeof ResultIntType);
    table dt = check dtRet;

    int value;
    while (dt.hasNext()) {
        ResultIntType rs = check <ResultIntType>dt.getNext();
        value = rs.INT_TYPE;
    }
    _ = testDB -> close();
    return value;
}

function testArrayInParameters () returns (int, map, map, map, map, map, map) {
    endpoint sql:Client testDB {
        url: "hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username: "SA",
        options: {maximumPoolSize:1}
    };

    int[] intArray = [1];
    int[] longArray = [1503383034226, 1503383034224, 1503383034225];
    float[] floatArray = [245.23, 5559.49, 8796.123];
    float[] doubleArray = [1503383034226.23, 1503383034224.43, 1503383034225.123];
    boolean[] boolArray = [true, false, true];
    string[] stringArray = ["Hello", "Ballerina"];
    sql:Parameter para1 = {sqlType:sql:TYPE_INTEGER, value:2};
    sql:Parameter para2 = {sqlType:sql:TYPE_ARRAY, value:intArray};
    sql:Parameter para3 = {sqlType:sql:TYPE_ARRAY, value:longArray};
    sql:Parameter para4 = {sqlType:sql:TYPE_ARRAY, value:floatArray};
    sql:Parameter para5 = {sqlType:sql:TYPE_ARRAY, value:doubleArray};
    sql:Parameter para6 = {sqlType:sql:TYPE_ARRAY, value:boolArray};
    sql:Parameter para7 = {sqlType:sql:TYPE_ARRAY, value:stringArray};
    sql:Parameter[] parameters = [para1, para2, para3, para4, para5, para6, para7];

    int insertCount;
    map int_arr;
    map long_arr;
    map double_arr;
    map string_arr;
    map boolean_arr;
    map float_arr;

    var insertCountRet = testDB -> update("INSERT INTO ArrayTypes (row_id, int_array, long_array,
        float_array, double_array, boolean_array, string_array) values (?,?,?,?,?,?,?)", parameters);
    insertCount = check insertCountRet;

    var dtRet = testDB -> select("SELECT int_array, long_array, double_array, boolean_array,
        string_array, float_array from ArrayTypes where row_id = 2", (), typeof ResultArrayType);
    table dt = check dtRet;

    while (dt.hasNext()) {
        ResultArrayType rs = check <ResultArrayType>dt.getNext();
        int_arr = rs.INT_ARRAY;
        long_arr = rs.LONG_ARRAY;
        double_arr = rs.DOUBLE_ARRAY;
        boolean_arr = rs.BOOLEAN_ARRAY;
        string_arr = rs.STRING_ARRAY;
        float_arr = rs.FLOAT_ARRAY;
    }
    _ = testDB -> close();
    return (insertCount, int_arr, long_arr, double_arr, string_arr, boolean_arr, float_arr);
}

function testOutParameters () returns (any, any, any, any, any, any, any, any, any, any, any, any, any, any) {
    endpoint sql:Client testDB {
        url: "hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username: "SA",
        options: {maximumPoolSize:1}
    };

    sql:Parameter paraID = {sqlType:sql:TYPE_INTEGER, value:"1"};
    sql:Parameter paraInt = {sqlType:sql:TYPE_INTEGER, direction:sql:DIRECTION_OUT};
    sql:Parameter paraLong = {sqlType:sql:TYPE_BIGINT, direction:sql:DIRECTION_OUT};
    sql:Parameter paraFloat = {sqlType:sql:TYPE_FLOAT, direction:sql:DIRECTION_OUT};
    sql:Parameter paraDouble = {sqlType:sql:TYPE_DOUBLE, direction:sql:DIRECTION_OUT};
    sql:Parameter paraBool = {sqlType:sql:TYPE_BOOLEAN, direction:sql:DIRECTION_OUT};
    sql:Parameter paraString = {sqlType:sql:TYPE_VARCHAR, direction:sql:DIRECTION_OUT};
    sql:Parameter paraNumeric = {sqlType:sql:TYPE_NUMERIC, direction:sql:DIRECTION_OUT};
    sql:Parameter paraDecimal = {sqlType:sql:TYPE_DECIMAL, direction:sql:DIRECTION_OUT};
    sql:Parameter paraReal = {sqlType:sql:TYPE_REAL, direction:sql:DIRECTION_OUT};
    sql:Parameter paraTinyInt = {sqlType:sql:TYPE_TINYINT, direction:sql:DIRECTION_OUT};
    sql:Parameter paraSmallInt = {sqlType:sql:TYPE_SMALLINT, direction:sql:DIRECTION_OUT};
    sql:Parameter paraClob = {sqlType:sql:TYPE_CLOB, direction:sql:DIRECTION_OUT};
    sql:Parameter paraBlob = {sqlType:sql:TYPE_BLOB, direction:sql:DIRECTION_OUT};
    sql:Parameter paraBinary = {sqlType:sql:TYPE_BINARY, direction:sql:DIRECTION_OUT};

    sql:Parameter[] parameters = [paraID, paraInt, paraLong, paraFloat, paraDouble, paraBool, paraString, paraNumeric,
    paraDecimal, paraReal, paraTinyInt, paraSmallInt, paraClob, paraBlob, paraBinary];

    _ = testDB -> call("{call TestOutParams(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}", parameters, ());

    _ = testDB -> close();

    return (paraInt.value, paraLong.value, paraFloat.value, paraDouble.value, paraBool.value, paraString.value,
    paraNumeric.value, paraDecimal.value, paraReal.value, paraTinyInt.value, paraSmallInt.value, paraClob.value,
    paraBlob.value, paraBinary.value);
}

function testNullOutParameters () returns (any, any, any, any, any, any, any, any, any, any, any, any, any, any) {
    endpoint sql:Client testDB {
        url: "hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username: "SA",
        options: {maximumPoolSize:1}
    };

    sql:Parameter paraID = {sqlType:sql:TYPE_INTEGER, value:"2"};
    sql:Parameter paraInt = {sqlType:sql:TYPE_INTEGER, direction:sql:DIRECTION_OUT};
    sql:Parameter paraLong = {sqlType:sql:TYPE_BIGINT, direction:sql:DIRECTION_OUT};
    sql:Parameter paraFloat = {sqlType:sql:TYPE_FLOAT, direction:sql:DIRECTION_OUT};
    sql:Parameter paraDouble = {sqlType:sql:TYPE_DOUBLE, direction:sql:DIRECTION_OUT};
    sql:Parameter paraBool = {sqlType:sql:TYPE_BOOLEAN, direction:sql:DIRECTION_OUT};
    sql:Parameter paraString = {sqlType:sql:TYPE_VARCHAR, direction:sql:DIRECTION_OUT};
    sql:Parameter paraNumeric = {sqlType:sql:TYPE_NUMERIC, direction:sql:DIRECTION_OUT};
    sql:Parameter paraDecimal = {sqlType:sql:TYPE_DECIMAL, direction:sql:DIRECTION_OUT};
    sql:Parameter paraReal = {sqlType:sql:TYPE_REAL, direction:sql:DIRECTION_OUT};
    sql:Parameter paraTinyInt = {sqlType:sql:TYPE_TINYINT, direction:sql:DIRECTION_OUT};
    sql:Parameter paraSmallInt = {sqlType:sql:TYPE_SMALLINT, direction:sql:DIRECTION_OUT};
    sql:Parameter paraClob = {sqlType:sql:TYPE_CLOB, direction:sql:DIRECTION_OUT};
    sql:Parameter paraBlob = {sqlType:sql:TYPE_BLOB, direction:sql:DIRECTION_OUT};
    sql:Parameter paraBinary = {sqlType:sql:TYPE_BINARY, direction:sql:DIRECTION_OUT};

    sql:Parameter[] parameters = [paraID, paraInt, paraLong, paraFloat, paraDouble, paraBool, paraString, paraNumeric,
                                  paraDecimal, paraReal, paraTinyInt, paraSmallInt, paraClob, paraBlob, paraBinary];
    _ = testDB -> call("{call TestOutParams(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}", parameters, ());
    _ = testDB -> close();
    return (paraInt.value, paraLong.value, paraFloat.value, paraDouble.value, paraBool.value, paraString.value,
           paraNumeric.value, paraDecimal.value, paraReal.value, paraTinyInt.value, paraSmallInt.value, paraClob.value,
           paraBlob.value, paraBinary.value);
}

function testINParameters () returns (int) {
    endpoint sql:Client testDB {
        url: "hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username: "SA",
        options: {maximumPoolSize:1}
    };

    sql:Parameter paraID = {sqlType:sql:TYPE_INTEGER, value:3};
    sql:Parameter paraInt = {sqlType:sql:TYPE_INTEGER, value:1};
    sql:Parameter paraLong = {sqlType:sql:TYPE_BIGINT, value:"9223372036854774807"};
    sql:Parameter paraFloat = {sqlType:sql:TYPE_FLOAT, value:123.34};
    sql:Parameter paraDouble = {sqlType:sql:TYPE_DOUBLE, value:2139095039};
    sql:Parameter paraBool = {sqlType:sql:TYPE_BOOLEAN, value:true};
    sql:Parameter paraString = {sqlType:sql:TYPE_VARCHAR, value:"Hello"};
    sql:Parameter paraNumeric = {sqlType:sql:TYPE_NUMERIC, value:1234.567};
    sql:Parameter paraDecimal = {sqlType:sql:TYPE_DECIMAL, value:1234.567};
    sql:Parameter paraReal = {sqlType:sql:TYPE_REAL, value:1234.567};
    sql:Parameter paraTinyInt = {sqlType:sql:TYPE_TINYINT, value:1};
    sql:Parameter paraSmallInt = {sqlType:sql:TYPE_SMALLINT, value:5555};
    sql:Parameter paraClob = {sqlType:sql:TYPE_CLOB, value:"very long text"};
    sql:Parameter paraBlob = {sqlType:sql:TYPE_BLOB, value:"YmxvYiBkYXRh"};
    sql:Parameter paraBinary = {sqlType:sql:TYPE_BINARY, value:"d3NvMiBiYWxsZXJpbmEgYmluYXJ5IHRlc3Qu"};

    sql:Parameter[] parameters = [paraID, paraInt, paraLong, paraFloat, paraDouble, paraBool, paraString, paraNumeric,
                                  paraDecimal, paraReal, paraTinyInt, paraSmallInt, paraClob, paraBlob, paraBinary];
    var insertCountRet = testDB -> update("INSERT INTO DataTypeTable (row_id,int_type, long_type,
            float_type, double_type, boolean_type, string_type, numeric_type, decimal_type, real_type, tinyint_type,
            smallint_type, clob_type, blob_type, binary_type) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", parameters);
    int insertCount = check insertCountRet;
    _ = testDB -> close();
    return insertCount;
}

function testNullINParameterValues () returns (int) {
    endpoint sql:Client testDB {
        url: "hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username: "SA",
        options: {maximumPoolSize:1}
    };

    sql:Parameter paraID = {sqlType:sql:TYPE_INTEGER, value:4};
    sql:Parameter paraInt = {sqlType:sql:TYPE_INTEGER};
    sql:Parameter paraLong = {sqlType:sql:TYPE_BIGINT};
    sql:Parameter paraFloat = {sqlType:sql:TYPE_FLOAT};
    sql:Parameter paraDouble = {sqlType:sql:TYPE_DOUBLE};
    sql:Parameter paraBool = {sqlType:sql:TYPE_BOOLEAN};
    sql:Parameter paraString = {sqlType:sql:TYPE_VARCHAR};
    sql:Parameter paraNumeric = {sqlType:sql:TYPE_NUMERIC};
    sql:Parameter paraDecimal = {sqlType:sql:TYPE_DECIMAL};
    sql:Parameter paraReal = {sqlType:sql:TYPE_REAL};
    sql:Parameter paraTinyInt = {sqlType:sql:TYPE_TINYINT};
    sql:Parameter paraSmallInt = {sqlType:sql:TYPE_SMALLINT};
    sql:Parameter paraClob = {sqlType:sql:TYPE_CLOB};
    sql:Parameter paraBlob = {sqlType:sql:TYPE_BLOB};
    sql:Parameter paraBinary = {sqlType:sql:TYPE_BINARY};

    sql:Parameter[] parameters = [paraID, paraInt, paraLong, paraFloat, paraDouble, paraBool, paraString, paraNumeric,
                                  paraDecimal, paraReal, paraTinyInt, paraSmallInt, paraClob, paraBlob, paraBinary];
    var insertCountRet = testDB -> update("INSERT INTO DataTypeTable (row_id, int_type, long_type,
            float_type, double_type, boolean_type, string_type, numeric_type, decimal_type, real_type, tinyint_type,
            smallint_type, clob_type, blob_type, binary_type) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", parameters);
    int insertCount = check insertCountRet;
    _ = testDB -> close();
    return insertCount;
}

function testINOutParameters () returns (any, any, any, any, any, any, any, any, any, any, any, any, any, any) {
    endpoint sql:Client testDB {
        url: "hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username: "SA",
        options: {maximumPoolSize:1}
    };

    sql:Parameter paraID = {sqlType:sql:TYPE_INTEGER, value:5};
    sql:Parameter paraInt = {sqlType:sql:TYPE_INTEGER, value:10, direction:sql:DIRECTION_INOUT};
    sql:Parameter paraLong = {sqlType:sql:TYPE_BIGINT, value:"9223372036854774807", direction:sql:DIRECTION_INOUT};
    sql:Parameter paraFloat = {sqlType:sql:TYPE_FLOAT, value:123.34, direction:sql:DIRECTION_INOUT};
    sql:Parameter paraDouble = {sqlType:sql:TYPE_DOUBLE, value:2139095039, direction:sql:DIRECTION_INOUT};
    sql:Parameter paraBool = {sqlType:sql:TYPE_BOOLEAN, value:true, direction:sql:DIRECTION_INOUT};
    sql:Parameter paraString = {sqlType:sql:TYPE_VARCHAR, value:"Hello", direction:sql:DIRECTION_INOUT};
    sql:Parameter paraNumeric = {sqlType:sql:TYPE_NUMERIC, value:1234.567, direction:sql:DIRECTION_INOUT};
    sql:Parameter paraDecimal = {sqlType:sql:TYPE_DECIMAL, value:1234.567, direction:sql:DIRECTION_INOUT};
    sql:Parameter paraReal = {sqlType:sql:TYPE_REAL, value:1234.567, direction:sql:DIRECTION_INOUT};
    sql:Parameter paraTinyInt = {sqlType:sql:TYPE_TINYINT, value:1, direction:sql:DIRECTION_INOUT};
    sql:Parameter paraSmallInt = {sqlType:sql:TYPE_SMALLINT, value:5555, direction:sql:DIRECTION_INOUT};
    sql:Parameter paraClob = {sqlType:sql:TYPE_CLOB, value:"very long text", direction:sql:DIRECTION_INOUT};
    sql:Parameter paraBlob = {sqlType:sql:TYPE_BLOB, value:"YmxvYiBkYXRh", direction:sql:DIRECTION_INOUT};
    sql:Parameter paraBinary = {sqlType:sql:TYPE_BINARY, value:"d3NvMiBiYWxsZXJpbmEgYmluYXJ5IHRlc3Qu", direction:sql:DIRECTION_INOUT};

    sql:Parameter[] parameters = [paraID, paraInt, paraLong, paraFloat, paraDouble, paraBool, paraString, paraNumeric,
                                  paraDecimal, paraReal, paraTinyInt, paraSmallInt, paraClob, paraBlob, paraBinary];
    _ = testDB -> call("{call TestINOUTParams(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}", parameters, ());
    _ = testDB -> close();
    return (paraInt.value, paraLong.value, paraFloat.value, paraDouble.value, paraBool.value, paraString.value,
           paraNumeric.value, paraDecimal.value, paraReal.value, paraTinyInt.value, paraSmallInt.value, paraClob.value,
           paraBlob.value, paraBinary.value);
}

function testNullINOutParameters () returns (any, any, any, any, any, any, any, any, any, any, any, any, any, any) {
    endpoint sql:Client testDB {
        url: "hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username: "SA",
        options: {maximumPoolSize:1}
    };

    sql:Parameter paraID = {sqlType:sql:TYPE_INTEGER, value:"6"};
    sql:Parameter paraInt = {sqlType:sql:TYPE_INTEGER, direction:sql:DIRECTION_INOUT};
    sql:Parameter paraLong = {sqlType:sql:TYPE_BIGINT, direction:sql:DIRECTION_INOUT};
    sql:Parameter paraFloat = {sqlType:sql:TYPE_FLOAT, direction:sql:DIRECTION_INOUT};
    sql:Parameter paraDouble = {sqlType:sql:TYPE_DOUBLE, direction:sql:DIRECTION_INOUT};
    sql:Parameter paraBool = {sqlType:sql:TYPE_BOOLEAN, direction:sql:DIRECTION_INOUT};
    sql:Parameter paraString = {sqlType:sql:TYPE_VARCHAR, direction:sql:DIRECTION_INOUT};
    sql:Parameter paraNumeric = {sqlType:sql:TYPE_NUMERIC, direction:sql:DIRECTION_INOUT};
    sql:Parameter paraDecimal = {sqlType:sql:TYPE_DECIMAL, direction:sql:DIRECTION_INOUT};
    sql:Parameter paraReal = {sqlType:sql:TYPE_REAL, direction:sql:DIRECTION_INOUT};
    sql:Parameter paraTinyInt = {sqlType:sql:TYPE_TINYINT, direction:sql:DIRECTION_INOUT};
    sql:Parameter paraSmallInt = {sqlType:sql:TYPE_SMALLINT, direction:sql:DIRECTION_INOUT};
    sql:Parameter paraClob = {sqlType:sql:TYPE_CLOB, direction:sql:DIRECTION_INOUT};
    sql:Parameter paraBlob = {sqlType:sql:TYPE_BLOB, direction:sql:DIRECTION_INOUT};
    sql:Parameter paraBinary = {sqlType:sql:TYPE_BINARY, direction:sql:DIRECTION_INOUT};

    sql:Parameter[] parameters = [paraID, paraInt, paraLong, paraFloat, paraDouble, paraBool, paraString, paraNumeric,
                                  paraDecimal, paraReal, paraTinyInt, paraSmallInt, paraClob, paraBlob, paraBinary];
    _ = testDB -> call("{call TestINOUTParams(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}", parameters, ());
    _ = testDB -> close();
    return (paraInt.value, paraLong.value, paraFloat.value, paraDouble.value, paraBool.value, paraString.value,
           paraNumeric.value, paraDecimal.value, paraReal.value, paraTinyInt.value, paraSmallInt.value, paraClob.value,
           paraBlob.value, paraBinary.value);
}

function testEmptySQLType () returns (int) {
    endpoint sql:Client testDB {
        url: "hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username: "SA",
        options: {maximumPoolSize:1}
    };

    sql:Parameter para1 = {value:"Anne"};
    sql:Parameter[] parameters = [para1];
    var insertCountRet = testDB -> update("Insert into Customers (firstName) values (?)", parameters);
    int insertCount = check insertCountRet;

    _ = testDB -> close();
    return insertCount;
}

function testArrayOutParameters () returns (any, any, any, any, any, any) {
    endpoint sql:Client testDB {
        url: "hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username: "SA",
        options: {maximumPoolSize:1}
    };

    string firstName;
    sql:Parameter para1 = {sqlType:sql:TYPE_ARRAY, direction:sql:DIRECTION_OUT};
    sql:Parameter para2 = {sqlType:sql:TYPE_ARRAY, direction:sql:DIRECTION_OUT};
    sql:Parameter para3 = {sqlType:sql:TYPE_ARRAY, direction:sql:DIRECTION_OUT};
    sql:Parameter para4 = {sqlType:sql:TYPE_ARRAY, direction:sql:DIRECTION_OUT};
    sql:Parameter para5 = {sqlType:sql:TYPE_ARRAY, direction:sql:DIRECTION_OUT};
    sql:Parameter para6 = {sqlType:sql:TYPE_ARRAY, direction:sql:DIRECTION_OUT};
    sql:Parameter[] parameters = [para1, para2, para3, para4, para5, para6];
    _ = testDB -> call("{call TestArrayOutParams(?,?,?,?,?,?)}", parameters, ());
    _ = testDB -> close();
    return (para1.value, para2.value, para3.value, para4.value, para5.value, para6.value);
}

function testArrayInOutParameters () returns (any, any, any, any, any, any, any) {
    endpoint sql:Client testDB {
        url: "hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username: "SA",
        options: {maximumPoolSize:1}
    };

    sql:Parameter para1 = {sqlType:sql:TYPE_INTEGER, value:3};
    sql:Parameter para2 = {sqlType:sql:TYPE_INTEGER, direction:sql:DIRECTION_OUT};
    sql:Parameter para3 = {sqlType:sql:TYPE_ARRAY, value:"10,20,30", direction:sql:DIRECTION_INOUT};
    sql:Parameter para4 = {sqlType:sql:TYPE_ARRAY, value:"10000000, 20000000, 30000000", direction:sql:DIRECTION_INOUT};
    sql:Parameter para5 = {sqlType:sql:TYPE_ARRAY, value:"2454.23, 55594.49, 87964.123", direction:sql:DIRECTION_INOUT};
    sql:Parameter para6 = {sqlType:sql:TYPE_ARRAY, value:"2454.23, 55594.49, 87964.123", direction:sql:DIRECTION_INOUT};
    sql:Parameter para7 = {sqlType:sql:TYPE_ARRAY, value:"FALSE, FALSE, TRUE", direction:sql:DIRECTION_INOUT};
    sql:Parameter para8 = {sqlType:sql:TYPE_ARRAY, value:"Hello,Ballerina,Lang", direction:sql:DIRECTION_INOUT};
    sql:Parameter[] parameters = [para1, para2, para3, para4, para5, para6, para7, para8];

    _ = testDB -> call("{call TestArrayInOutParams(?,?,?,?,?,?,?,?)}", parameters, ());
    _ = testDB -> close();
    return (para2.value, para3.value, para4.value, para5.value, para6.value, para7.value, para8.value);
}

function testBatchUpdate () returns (int[]) {
    endpoint sql:Client testDB {
        url: "hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username: "SA",
        options: {maximumPoolSize:1}
    };

    //Batch 1
    sql:Parameter para1 = {sqlType:sql:TYPE_VARCHAR, value:"Alex"};
    sql:Parameter para2 = {sqlType:sql:TYPE_VARCHAR, value:"Smith"};
    sql:Parameter para3 = {sqlType:sql:TYPE_INTEGER, value:20};
    sql:Parameter para4 = {sqlType:sql:TYPE_DOUBLE, value:3400.5};
    sql:Parameter para5 = {sqlType:sql:TYPE_VARCHAR, value:"Colombo"};
    sql:Parameter[] parameters1 = [para1, para2, para3, para4, para5];

    //Batch 2
    para1 = {sqlType:sql:TYPE_VARCHAR, value:"Alex"};
    para2 = {sqlType:sql:TYPE_VARCHAR, value:"Smith"};
    para3 = {sqlType:sql:TYPE_INTEGER, value:20};
    para4 = {sqlType:sql:TYPE_DOUBLE, value:3400.5};
    para5 = {sqlType:sql:TYPE_VARCHAR, value:"Colombo"};
    sql:Parameter[] parameters2 = [para1, para2, para3, para4, para5];
    sql:Parameter[][] parameters = [parameters1, parameters2];

    int[] updateCount;
    var updateCountRet = testDB -> batchUpdate("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                                     values (?,?,?,?,?)", parameters);
    updateCount = check updateCountRet;

    _ = testDB -> close();
    return updateCount;
}

function testBatchUpdateWithFailure () returns (int[], int) {
    endpoint sql:Client testDB {
        url: "hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username: "SA",
        options: {maximumPoolSize:1}
    };

    //Batch 1
    sql:Parameter para0 = {sqlType:sql:TYPE_INTEGER, value:111};
    sql:Parameter para1 = {sqlType:sql:TYPE_VARCHAR, value:"Alex"};
    sql:Parameter para2 = {sqlType:sql:TYPE_VARCHAR, value:"Smith"};
    sql:Parameter para3 = {sqlType:sql:TYPE_INTEGER, value:20};
    sql:Parameter para4 = {sqlType:sql:TYPE_DOUBLE, value:3400.5};
    sql:Parameter para5 = {sqlType:sql:TYPE_VARCHAR, value:"Colombo"};
    sql:Parameter[] parameters1 = [para0, para1, para2, para3, para4, para5];

    //Batch 2
    para0 = {sqlType:sql:TYPE_INTEGER, value:222};
    para1 = {sqlType:sql:TYPE_VARCHAR, value:"Alex"};
    para2 = {sqlType:sql:TYPE_VARCHAR, value:"Smith"};
    para3 = {sqlType:sql:TYPE_INTEGER, value:20};
    para4 = {sqlType:sql:TYPE_DOUBLE, value:3400.5};
    para5 = {sqlType:sql:TYPE_VARCHAR, value:"Colombo"};
    sql:Parameter[] parameters2 = [para0, para1, para2, para3, para4, para5];

    //Batch 3
    para0 = {sqlType:sql:TYPE_INTEGER, value:222};
    para1 = {sqlType:sql:TYPE_VARCHAR, value:"Alex"};
    para2 = {sqlType:sql:TYPE_VARCHAR, value:"Smith"};
    para3 = {sqlType:sql:TYPE_INTEGER, value:20};
    para4 = {sqlType:sql:TYPE_DOUBLE, value:3400.5};
    para5 = {sqlType:sql:TYPE_VARCHAR, value:"Colombo"};
    sql:Parameter[] parameters3 = [para0, para1, para2, para3, para4, para5];

    //Batch 4
    para0 = {sqlType:sql:TYPE_INTEGER, value:333};
    para1 = {sqlType:sql:TYPE_VARCHAR, value:"Alex"};
    para2 = {sqlType:sql:TYPE_VARCHAR, value:"Smith"};
    para3 = {sqlType:sql:TYPE_INTEGER, value:20};
    para4 = {sqlType:sql:TYPE_DOUBLE, value:3400.5};
    para5 = {sqlType:sql:TYPE_VARCHAR, value:"Colombo"};
    sql:Parameter[] parameters4 = [para0, para1, para2, para3, para4, para5];

    sql:Parameter[][] parameters = [parameters1, parameters2, parameters3, parameters4];

    int[] updateCount;
    int count;

    var updateCountRet = testDB -> batchUpdate("Insert into Customers (customerId, firstName,lastName,registrationID,creditLimit,
        country) values (?,?,?,?,?,?)", parameters);
    updateCount = check updateCountRet;

    var dtRet = testDB -> select("SELECT count(*) as countval from Customers where customerId in (111,222,333)", (), typeof ResultCount);
    table dt = check dtRet;

    while (dt.hasNext()) {
        var rs = check <ResultCount>dt.getNext();
        count = rs.COUNTVAL;
    }

    _ = testDB -> close();
    return (updateCount, count);
}

function testBatchUpdateWithNullParam () returns (int[]) {
    endpoint sql:Client testDB {
        url: "hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username: "SA",
        options: {maximumPoolSize:1}
    };

    int[] updateCount;
    var updateCountRet = testDB -> batchUpdate("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                                     values ('Alex','Smith',20,3400.5,'Colombo')", ());
    updateCount = check updateCountRet;

    _ = testDB -> close();
    return updateCount;
}

function testDateTimeInParameters () returns (int[]) {
    endpoint sql:Client testDB {
        url: "hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username: "SA",
        options: {maximumPoolSize:1}
    };

    string stmt = "Insert into DateTimeTypes(row_id,date_type,time_type,datetime_type,timestamp_type) values (?,?,?,?,?)";
    int[] returnValues = [];
    sql:Parameter para1 = {sqlType:sql:TYPE_INTEGER, value:100};
    sql:Parameter para2 = {sqlType:sql:TYPE_DATE, value:"2017-01-30-08:01"};
    sql:Parameter para3 = {sqlType:sql:TYPE_TIME, value:"13:27:01.999999+08:33"};
    sql:Parameter para4 = {sqlType:sql:TYPE_TIMESTAMP, value:"2017-01-30T13:27:01.999-08:00"};
    sql:Parameter para5 = {sqlType:sql:TYPE_DATETIME, value:"2017-01-30T13:27:01.999999Z"};
    sql:Parameter[] parameters = [para1, para2, para3, para4, para5];

    var insertCount1Ret = testDB -> update(stmt, parameters);
    int insertCount1 = check insertCount1Ret;

    returnValues[0] = insertCount1;

    para1 = {sqlType:sql:TYPE_INTEGER, value:200};
    para2 = {sqlType:sql:TYPE_DATE, value:"-2017-01-30Z"};
    para3 = {sqlType:sql:TYPE_TIME, value:"13:27:01+08:33"};
    para4 = {sqlType:sql:TYPE_TIMESTAMP, value:"2017-01-30T13:27:01.999"};
    para5 = {sqlType:sql:TYPE_DATETIME, value:"-2017-01-30T13:27:01.999999-08:30"};
    parameters = [para1, para2, para3, para4, para5];

    var insertCount2Ret = testDB -> update(stmt, parameters);
    int insertCount2 = check insertCount2Ret;

    returnValues[1] = insertCount2;

    time:Time timeNow = time:currentTime();
    para1 = {sqlType:sql:TYPE_INTEGER, value:300};
    para2 = {sqlType:sql:TYPE_DATE, value:timeNow};
    para3 = {sqlType:sql:TYPE_TIME, value:timeNow};
    para4 = {sqlType:sql:TYPE_TIMESTAMP, value:timeNow};
    para5 = {sqlType:sql:TYPE_DATETIME, value:timeNow};
    parameters = [para1, para2, para3, para4, para5];

    var insertCount3Ret = testDB -> update(stmt, parameters);
    int insertCount3 = check insertCount3Ret;

    returnValues[2] = insertCount3;

    _ = testDB -> close();
    return returnValues;
}

function testDateTimeNullInValues () returns (string) {
    endpoint sql:Client testDB {
        url: "hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username: "SA",
        options: {maximumPoolSize:1}
    };

    sql:Parameter para0 = {sqlType:sql:TYPE_INTEGER, value:33};
    sql:Parameter para1 = {sqlType:sql:TYPE_DATE, value:()};
    sql:Parameter para2 = {sqlType:sql:TYPE_TIME, value:()};
    sql:Parameter para3 = {sqlType:sql:TYPE_TIMESTAMP, value:()};
    sql:Parameter para4 = {sqlType:sql:TYPE_DATETIME, value:()};
    sql:Parameter[] parameters = [para0, para1, para2, para3, para4];

    _ = testDB -> update("Insert into DateTimeTypes
        (row_id, date_type, time_type, timestamp_type, datetime_type) values (?,?,?,?,?)", parameters);

    var dtRet = testDB -> select("SELECT date_type, time_type, timestamp_type, datetime_type
                from DateTimeTypes where row_id = 33", (), typeof ResultDates);
    table dt = check dtRet;

    string data;

    var j = check  <json>dt;
    data = io:sprintf("%j", [j]);

    _ = testDB -> close();
    return data;
}


function testDateTimeNullOutValues () returns (int) {
    endpoint sql:Client testDB {
        url: "hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username: "SA",
        options: {maximumPoolSize:1}
    };

    sql:Parameter para1 = {sqlType:sql:TYPE_INTEGER, value:123};
    sql:Parameter para2 = {sqlType:sql:TYPE_DATE, value:()};
    sql:Parameter para3 = {sqlType:sql:TYPE_TIME, value:()};
    sql:Parameter para4 = {sqlType:sql:TYPE_TIMESTAMP, value:()};
    sql:Parameter para5 = {sqlType:sql:TYPE_DATETIME, value:()};

    sql:Parameter para6 = {sqlType:sql:TYPE_DATE, direction:sql:DIRECTION_OUT};
    sql:Parameter para7 = {sqlType:sql:TYPE_TIME, direction:sql:DIRECTION_OUT};
    sql:Parameter para8 = {sqlType:sql:TYPE_TIMESTAMP, direction:sql:DIRECTION_OUT};
    sql:Parameter para9 = {sqlType:sql:TYPE_DATETIME, direction:sql:DIRECTION_OUT};

    sql:Parameter[] parameters = [para1, para2, para3, para4, para5, para6, para7, para8, para9];

    _ = testDB -> call("{call TestDateTimeOutParams(?,?,?,?,?,?,?,?,?)}", parameters, ());

    var dtRet = testDB -> select("SELECT count(*) as countval from DateTimeTypes where row_id = 123", (),
                             typeof ResultCount);
    table dt = check dtRet;

    int count;
    while (dt.hasNext()) {
        ResultCount rs = check <ResultCount>dt.getNext();
        count = rs.COUNTVAL;
    }
    _ = testDB -> close();
    return count;
}

function testDateTimeNullInOutValues () returns (any, any, any, any) {
    endpoint sql:Client testDB {
        url: "hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username: "SA",
        options: {maximumPoolSize:1}
    };

    sql:Parameter para1 = {sqlType:sql:TYPE_INTEGER, value:124};
    sql:Parameter para2 = {sqlType:sql:TYPE_DATE, value:null, direction:sql:DIRECTION_INOUT};
    sql:Parameter para3 = {sqlType:sql:TYPE_TIME, value:null, direction:sql:DIRECTION_INOUT};
    sql:Parameter para4 = {sqlType:sql:TYPE_TIMESTAMP, value:null, direction:sql:DIRECTION_INOUT};
    sql:Parameter para5 = {sqlType:sql:TYPE_DATETIME, value:null, direction:sql:DIRECTION_INOUT};

    sql:Parameter[] parameters = [para1, para2, para3, para4, para5];

    _ = testDB -> call("{call TestDateINOUTParams(?,?,?,?,?)}", parameters, ());
    _ = testDB -> close();
    return (para2.value, para3.value, para4.value, para5.value);
}

function testDateTimeOutParams (int time, int date, int timestamp) returns (int) {
    endpoint sql:Client testDB {
        url: "hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username: "SA",
        options: {maximumPoolSize:1}
    };

    sql:Parameter para1 = {sqlType:sql:TYPE_INTEGER, value:10};
    sql:Parameter para2 = {sqlType:sql:TYPE_DATE, value:date};
    sql:Parameter para3 = {sqlType:sql:TYPE_TIME, value:time};
    sql:Parameter para4 = {sqlType:sql:TYPE_TIMESTAMP, value:timestamp};
    sql:Parameter para5 = {sqlType:sql:TYPE_DATETIME, value:timestamp};

    sql:Parameter para6 = {sqlType:sql:TYPE_DATE, direction:sql:DIRECTION_OUT};
    sql:Parameter para7 = {sqlType:sql:TYPE_TIME, direction:sql:DIRECTION_OUT};
    sql:Parameter para8 = {sqlType:sql:TYPE_TIMESTAMP, direction:sql:DIRECTION_OUT};
    sql:Parameter para9 = {sqlType:sql:TYPE_DATETIME, direction:sql:DIRECTION_OUT};

    sql:Parameter[] parameters = [para1, para2, para3, para4, para5, para6, para7, para8, para9];

    _ = testDB -> call("{call TestDateTimeOutParams(?,?,?,?,?,?,?,?,?)}", parameters, ());

    var dtRet = testDB -> select("SELECT count(*) as countval from DateTimeTypes where row_id = 10", (),
                             typeof ResultCount);
    table dt = check dtRet;

    int count;
    while (dt.hasNext()) {
        ResultCount rs = check <ResultCount>dt.getNext();
        count = rs.COUNTVAL;
    }
    _ = testDB -> close();
    return count;
}

function testStructOutParameters () returns (any) {
    endpoint sql:Client testDB {
        url: "hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username: "SA",
        options: {maximumPoolSize:1}
    };

    sql:Parameter para1 = {sqlType:sql:TYPE_STRUCT, direction:sql:DIRECTION_OUT};
    sql:Parameter[] parameters = [para1];
    _ = testDB -> call("{call TestStructOut(?)}", parameters, ());
    _ = testDB -> close();
    return para1.value;
}

function testComplexTypeRetrieval () returns (string, string, string, string) {
    endpoint sql:Client testDB {
        url: "hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username: "SA",
        options: {maximumPoolSize:1}
    };

    string s1;
    string s2;
    string s3;
    string s4;

    var dtRet = testDB -> select("SELECT * from DataTypeTable where row_id = 1", (), ());
    table dt = check dtRet;
    var x1 = check <xml>dt;
    s1 = io:sprintf("%l", [x1]);

    dtRet = testDB -> select("SELECT * from DateTimeTypes where row_id = 1", (), ());
    dt = check dtRet;
    var x2 = check <xml>dt;
    s2 = io:sprintf("%l", [x2]);

    dtRet = testDB -> select("SELECT * from DataTypeTable where row_id = 1", (), ());
    dt = check dtRet;
    var j = check <json>dt;
    s3 = io:sprintf("%j", [j]);

    dtRet = testDB -> select("SELECT * from DateTimeTypes where row_id = 1", (), ());
    dt = check dtRet;
    j = check <json>dt;
    s4 = io:sprintf("%j", [j]);

    _ = testDB -> close();
    return (s1, s2, s3, s4);
}

function testCloseConnectionPool () returns (int) {
    endpoint sql:Client testDB {
        url: "hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username: "SA",
        options: {maximumPoolSize:1}
    };

    var dtRet = testDB -> select("SELECT COUNT(*) as countVal FROM INFORMATION_SCHEMA.SYSTEM_SESSIONS", (),
        typeof ResultCount);
    table dt = check dtRet;

    int count;
    while (dt.hasNext()) {
        ResultCount rs = check <ResultCount>dt.getNext();
        count = rs.COUNTVAL;
    }
    _ = testDB -> close();
    return count;
}
