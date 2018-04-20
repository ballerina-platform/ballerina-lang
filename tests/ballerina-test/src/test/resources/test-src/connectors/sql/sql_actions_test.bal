import ballerina/sql;
import ballerina/jdbc;
import ballerina/time;
import ballerina/io;

type ResultCustomers {
    string FIRSTNAME,
};

type ResultCustomers2 {
    string FIRSTNAME,
    string LASTNAME,
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

type Employee {
    int id,
    string name,
    string address,
};


function testInsertTableData() returns (int) {
    endpoint jdbc:Client testDB {
        url:"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
    };

    var insertCount = check testDB->update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                                         values ('James', 'Clerk', 2, 5000.75, 'USA')");
    testDB.stop();
    return insertCount;
}

function testCreateTable() returns (int) {
    endpoint jdbc:Client testDB {
        url:"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
    };

    int returnValue = check testDB->update("CREATE TABLE IF NOT EXISTS Students(studentID int, LastName varchar(255))");
    testDB.stop();
    return returnValue;
}

function testUpdateTableData() returns (int) {
    endpoint jdbc:Client testDB {
        url:"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
    };

    var updateCount = check testDB->update("Update Customers set country = 'UK' where registrationID = 1");
    testDB.stop();
    return updateCount;
}

function testGeneratedKeyOnInsert() returns (string) {
    endpoint jdbc:Client testDB {
        url:"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
    };

    string returnVal;

    var x = testDB->updateWithGeneratedKeys("insert into Customers (firstName,lastName,
            registrationID,creditLimit,country) values ('Mary', 'Williams', 3, 5000.75, 'USA')", ());

    match x {
        (int, string[]) y => {
            int a;
            string[] b;
            (a, b) = y;
            returnVal = b[0];
        }
        error err1 => {
            returnVal = err1.message;
        }
    }

    testDB.stop();
    return returnVal;
}

function testGeneratedKeyWithColumn() returns (string) {
    endpoint jdbc:Client testDB {
        url:"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
    };

    int insertCount;
    string[] generatedID;
    string[] keyColumns = ["CUSTOMERID"];
    string returnVal;
    var x = testDB->updateWithGeneratedKeys("insert into Customers (firstName,lastName,
                               registrationID,creditLimit,country) values ('Kathy', 'Williams', 4, 5000.75, 'USA')",
        keyColumns);
    match x {
        (int, string[]) y => {
            int a;
            string[] b;
            (a, b) = y;
            returnVal = b[0];
        }
        error err1 => {
            returnVal = err1.message;
        }
    }

    testDB.stop();
    return returnVal;
}

function testSelectData() returns (string) {
    endpoint jdbc:Client testDB {
        url:"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
    };

    table dt = check testDB->select("SELECT  FirstName from Customers where registrationID = 1", ResultCustomers);
    string firstName;

    while (dt.hasNext()) {
        ResultCustomers rs = check <ResultCustomers>dt.getNext();
        firstName = rs.FIRSTNAME;
    }
    testDB.stop();
    return firstName;
}

function testSelectIntFloatData() returns (int, int, float, float) {
    endpoint jdbc:Client testDB {
        url:"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
    };

    table dt = check testDB->select("SELECT  int_type, long_type, float_type, double_type from DataTypeTable
        where row_id = 1", ResultDataType);
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
    testDB.stop();
    return (int_type, long_type, float_type, double_type);
}

function testCallProcedure() returns (string) {
    endpoint jdbc:Client testDB {
        url:"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
    };

    _ = testDB->call("{call InsertPersonData(100,'James')}", ());
    table dt = check testDB->select("SELECT  FirstName from Customers where registrationID = 100", ResultCustomers);
    string firstName;
    while (dt.hasNext()) {
        ResultCustomers rs = check <ResultCustomers>dt.getNext();
        firstName = rs.FIRSTNAME;
    }
    testDB.stop();
    return firstName;
}

function testCallProcedureWithResultSet() returns (string) {
    endpoint jdbc:Client testDB {
        url:"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
    };

    table[] dts = check testDB->call("{call SelectPersonData()}", [ResultCustomers]);

    string firstName;
    while (dts[0].hasNext()) {
        ResultCustomers rs = check <ResultCustomers>dts[0].getNext();
        firstName = rs.FIRSTNAME;
    }
    testDB.stop();
    return firstName;
}

function testCallProcedureWithMultipleResultSets() returns (string, string, string) {
    endpoint jdbc:Client testDB {
        url:"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
    };

    table[] dts = check testDB->call("{call SelectPersonDataMultiple()}", [ResultCustomers, ResultCustomers2]);

    string firstName1;
    string firstName2;
    string lastName;

    while (dts[0].hasNext()) {
        ResultCustomers rs = check <ResultCustomers>dts[0].getNext();
        firstName1 = rs.FIRSTNAME;
    }

    while (dts[1].hasNext()) {
        ResultCustomers2 rs = check <ResultCustomers2>dts[1].getNext();
        firstName2 = rs.FIRSTNAME;
        lastName = rs.LASTNAME;
    }

    testDB.stop();
    return (firstName1, firstName2, lastName);
}

function testQueryParameters() returns (string) {
    endpoint jdbc:Client testDB {
        url:"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
    };

    table dt = check testDB->select("SELECT  FirstName from Customers where registrationID = ?", ResultCustomers, 1);

    string firstName;

    while (dt.hasNext()) {
        ResultCustomers rs = check <ResultCustomers>dt.getNext();
        firstName = rs.FIRSTNAME;
    }
    testDB.stop();
    return firstName;
}

function testQueryParameters2() returns (string) {
    endpoint jdbc:Client testDB {
        url:"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
    };

    sql:Parameter p1 = {sqlType:sql:TYPE_INTEGER, value:1};
    table dt = check testDB->select("SELECT  FirstName from Customers where registrationID = ?", ResultCustomers,
         p1);

    string firstName;

    while (dt.hasNext()) {
        ResultCustomers rs = check <ResultCustomers>dt.getNext();
        firstName = rs.FIRSTNAME;
    }
    testDB.stop();
    return firstName;
}

function testInsertTableDataWithParameters() returns (int) {
    endpoint jdbc:Client testDB {
        url:"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
    };

    string s1 = "Anne";
    sql:Parameter para1 = {sqlType:sql:TYPE_VARCHAR, value:s1, direction:sql:DIRECTION_IN};
    sql:Parameter para2 = {sqlType:sql:TYPE_VARCHAR, value:"James", direction:sql:DIRECTION_IN};
    sql:Parameter para3 = {sqlType:sql:TYPE_INTEGER, value:3, direction:sql:DIRECTION_IN};
    sql:Parameter para4 = {sqlType:sql:TYPE_DOUBLE, value:5000.75, direction:sql:DIRECTION_IN};
    sql:Parameter para5 = {sqlType:sql:TYPE_VARCHAR, value:"UK", direction:sql:DIRECTION_IN};

    int insertCount = check testDB->update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                                     values (?,?,?,?,?)", para1, para2, para3, para4, para5);
    testDB.stop();
    return insertCount;
}

function testInsertTableDataWithParameters2() returns (int) {
    endpoint jdbc:Client testDB {
        url:"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
    };

    int insertCount = check testDB->update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                                     values (?,?,?,?,?)", "Anne", "James", 3, 5000.75, "UK");

    testDB.stop();
    return insertCount;
}

function testInsertTableDataWithParameters3() returns (int) {
    endpoint jdbc:Client testDB {
        url:"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
    };

    string s1 = "Anne";
    int insertCount = check testDB->update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                                     values (?,?,?,?,?)", s1, "James", 3, 5000.75, "UK");

    testDB.stop();
    return insertCount;
}

function testArrayofQueryParameters() returns (string) {
    endpoint jdbc:Client testDB {
        url:"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
    };

    int[] intDataArray = [1, 4343];
    string[] stringDataArray = ["A", "B"];
    float[] doubleArray = [233.4, 433.4];
    sql:Parameter para0 = {sqlType:sql:TYPE_VARCHAR, value:"Johhhn"};
    sql:Parameter para1 = {sqlType:sql:TYPE_INTEGER, value:intDataArray};
    sql:Parameter para2 = {sqlType:sql:TYPE_VARCHAR, value:stringDataArray};
    sql:Parameter para3 = {sqlType:sql:TYPE_DOUBLE, value:doubleArray};

    table dt = check testDB->select("SELECT  FirstName from Customers where FirstName = ? or lastName = 'A' or
        lastName = '\"BB\"' or registrationID in(?) or lastName in(?) or creditLimit in(?)", ResultCustomers,
        para0, para1, para2, para3);

    string firstName;
    while (dt.hasNext()) {
        ResultCustomers rs = check <ResultCustomers>dt.getNext();
        firstName = rs.FIRSTNAME;
    }
    testDB.stop();
    return firstName;
}

function testBoolArrayofQueryParameters() returns (int) {
    endpoint jdbc:Client testDB {
        url:"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
    };

    boolean accepted1 = false;
    boolean accepted2 = false;
    boolean accepted3 = true;
    boolean[] boolDataArray = [accepted1, accepted2, accepted3];

    table dt1 = check testDB->select("SELECT blob_type from DataTypeTable where row_id = 1", ResultBlob);

    blob blobData;
    while (dt1.hasNext()) {
        ResultBlob rs = check <ResultBlob>dt1.getNext();
        blobData = rs.BLOB_TYPE;
    }
    blob[] blobDataArray = [blobData];

    sql:Parameter para1 = {sqlType:sql:TYPE_BOOLEAN, value:boolDataArray};
    sql:Parameter para2 = {sqlType:sql:TYPE_BLOB, value:blobDataArray};

    table dt = check testDB->select("SELECT  int_type from DataTypeTable where row_id = ? and boolean_type in(?) and
        blob_type in (?)", ResultIntType, 1, para1, para2);

    int value;
    while (dt.hasNext()) {
        ResultIntType rs = check <ResultIntType>dt.getNext();
        value = rs.INT_TYPE;
    }
    testDB.stop();
    return value;
}

function testArrayInParameters() returns (int, map, map, map, map, map, map) {
    endpoint jdbc:Client testDB {
        url:"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
    };

    int[] intArray = [1];
    int[] longArray = [1503383034226, 1503383034224, 1503383034225];
    float[] floatArray = [245.23, 5559.49, 8796.123];
    float[] doubleArray = [1503383034226.23, 1503383034224.43, 1503383034225.123];
    boolean[] boolArray = [true, false, true];
    string[] stringArray = ["Hello", "Ballerina"];
    sql:Parameter para2 = {sqlType:sql:TYPE_ARRAY, value:intArray};
    sql:Parameter para3 = {sqlType:sql:TYPE_ARRAY, value:longArray};
    sql:Parameter para4 = {sqlType:sql:TYPE_ARRAY, value:floatArray};
    sql:Parameter para5 = {sqlType:sql:TYPE_ARRAY, value:doubleArray};
    sql:Parameter para6 = {sqlType:sql:TYPE_ARRAY, value:boolArray};
    sql:Parameter para7 = {sqlType:sql:TYPE_ARRAY, value:stringArray};

    int insertCount;
    map int_arr;
    map long_arr;
    map double_arr;
    map string_arr;
    map boolean_arr;
    map float_arr;

    insertCount = check testDB->update("INSERT INTO ArrayTypes (row_id, int_array, long_array,
        float_array, double_array, boolean_array, string_array) values (?,?,?,?,?,?,?)", 2, para2, para3, para4,
        para5, para6, para7);

    table dt = check testDB->select("SELECT int_array, long_array, double_array, boolean_array,
        string_array, float_array from ArrayTypes where row_id = 2", ResultArrayType);

    while (dt.hasNext()) {
        ResultArrayType rs = check <ResultArrayType>dt.getNext();
        int_arr = rs.INT_ARRAY;
        long_arr = rs.LONG_ARRAY;
        double_arr = rs.DOUBLE_ARRAY;
        boolean_arr = rs.BOOLEAN_ARRAY;
        string_arr = rs.STRING_ARRAY;
        float_arr = rs.FLOAT_ARRAY;
    }
    testDB.stop();
    return (insertCount, int_arr, long_arr, double_arr, string_arr, boolean_arr, float_arr);
}

function testOutParameters() returns (any, any, any, any, any, any, any, any, any, any, any, any, any, any) {
    endpoint jdbc:Client testDB {
        url:"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
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

    _ = testDB->call("{call TestOutParams(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}", (),
        paraID, paraInt, paraLong, paraFloat, paraDouble, paraBool, paraString, paraNumeric,
        paraDecimal, paraReal, paraTinyInt, paraSmallInt, paraClob, paraBlob, paraBinary);

    testDB.stop();

    return (paraInt.value, paraLong.value, paraFloat.value, paraDouble.value, paraBool.value, paraString.value,
    paraNumeric.value, paraDecimal.value, paraReal.value, paraTinyInt.value, paraSmallInt.value, paraClob.value,
    paraBlob.value, paraBinary.value);
}

function testNullOutParameters() returns (any, any, any, any, any, any, any, any, any, any, any, any, any, any) {
    endpoint jdbc:Client testDB {
        url:"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
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

    _ = testDB->call("{call TestOutParams(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}", (),
        paraID, paraInt, paraLong, paraFloat, paraDouble, paraBool, paraString, paraNumeric,
        paraDecimal, paraReal, paraTinyInt, paraSmallInt, paraClob, paraBlob, paraBinary);
    testDB.stop();
    return (paraInt.value, paraLong.value, paraFloat.value, paraDouble.value, paraBool.value, paraString.value,
    paraNumeric.value, paraDecimal.value, paraReal.value, paraTinyInt.value, paraSmallInt.value, paraClob.value,
    paraBlob.value, paraBinary.value);
}

function testINParameters() returns (int) {
    endpoint jdbc:Client testDB {
        url:"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
    };

    sql:Parameter paraID = {sqlType:sql:TYPE_INTEGER, value:3};
    sql:Parameter paraInt = {sqlType:sql:TYPE_INTEGER, value:1};
    sql:Parameter paraLong = {sqlType:sql:TYPE_BIGINT, value:9223372036854774807};
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

    int insertCount = check testDB->update("INSERT INTO DataTypeTable (row_id,int_type, long_type,
            float_type, double_type, boolean_type, string_type, numeric_type, decimal_type, real_type, tinyint_type,
            smallint_type, clob_type, blob_type, binary_type) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
        paraID, paraInt, paraLong, paraFloat, paraDouble, paraBool, paraString, paraNumeric,
        paraDecimal, paraReal, paraTinyInt, paraSmallInt, paraClob, paraBlob, paraBinary);
    testDB.stop();
    return insertCount;
}

function testNullINParameterValues() returns (int) {
    endpoint jdbc:Client testDB {
        url:"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
    };

    sql:Parameter paraID = {sqlType:sql:TYPE_INTEGER, value:4};
    sql:Parameter paraInt = {sqlType:sql:TYPE_INTEGER, value:()};
    sql:Parameter paraLong = {sqlType:sql:TYPE_BIGINT, value:()};
    sql:Parameter paraFloat = {sqlType:sql:TYPE_FLOAT, value:()};
    sql:Parameter paraDouble = {sqlType:sql:TYPE_DOUBLE, value:()};
    sql:Parameter paraBool = {sqlType:sql:TYPE_BOOLEAN, value:()};
    sql:Parameter paraString = {sqlType:sql:TYPE_VARCHAR, value:()};
    sql:Parameter paraNumeric = {sqlType:sql:TYPE_NUMERIC, value:()};
    sql:Parameter paraDecimal = {sqlType:sql:TYPE_DECIMAL, value:()};
    sql:Parameter paraReal = {sqlType:sql:TYPE_REAL, value:()};
    sql:Parameter paraTinyInt = {sqlType:sql:TYPE_TINYINT, value:()};
    sql:Parameter paraSmallInt = {sqlType:sql:TYPE_SMALLINT, value:()};
    sql:Parameter paraClob = {sqlType:sql:TYPE_CLOB, value:()};
    sql:Parameter paraBlob = {sqlType:sql:TYPE_BLOB, value:()};
    sql:Parameter paraBinary = {sqlType:sql:TYPE_BINARY, value:()};

    int insertCount = check testDB->update("INSERT INTO DataTypeTable (row_id, int_type, long_type,
            float_type, double_type, boolean_type, string_type, numeric_type, decimal_type, real_type, tinyint_type,
            smallint_type, clob_type, blob_type, binary_type) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
        paraID, paraInt, paraLong, paraFloat, paraDouble, paraBool, paraString, paraNumeric,
        paraDecimal, paraReal, paraTinyInt, paraSmallInt, paraClob, paraBlob, paraBinary);
    testDB.stop();
    return insertCount;
}

function testINOutParameters() returns (any, any, any, any, any, any, any, any, any, any, any, any, any, any) {
    endpoint jdbc:Client testDB {
        url:"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
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
    sql:Parameter paraBinary = {sqlType:sql:TYPE_BINARY, value:"d3NvMiBiYWxsZXJpbmEgYmluYXJ5IHRlc3Qu", direction:sql:
    DIRECTION_INOUT};

    _ = testDB->call("{call TestINOUTParams(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}", (),
        paraID, paraInt, paraLong, paraFloat, paraDouble, paraBool, paraString, paraNumeric,
        paraDecimal, paraReal, paraTinyInt, paraSmallInt, paraClob, paraBlob, paraBinary);
    testDB.stop();
    return (paraInt.value, paraLong.value, paraFloat.value, paraDouble.value, paraBool.value, paraString.value,
    paraNumeric.value, paraDecimal.value, paraReal.value, paraTinyInt.value, paraSmallInt.value, paraClob.value,
    paraBlob.value, paraBinary.value);
}

function testNullINOutParameters() returns (any, any, any, any, any, any, any, any, any, any, any, any, any, any) {
    endpoint jdbc:Client testDB {
        url:"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
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

    _ = testDB->call("{call TestINOUTParams(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}", (),
        paraID, paraInt, paraLong, paraFloat, paraDouble, paraBool, paraString, paraNumeric,
        paraDecimal, paraReal, paraTinyInt, paraSmallInt, paraClob, paraBlob, paraBinary);
    testDB.stop();
    return (paraInt.value, paraLong.value, paraFloat.value, paraDouble.value, paraBool.value, paraString.value,
    paraNumeric.value, paraDecimal.value, paraReal.value, paraTinyInt.value, paraSmallInt.value, paraClob.value,
    paraBlob.value, paraBinary.value);
}

function testEmptySQLType() returns (int) {
    endpoint jdbc:Client testDB {
        url:"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
    };

    int insertCount = check testDB->update("Insert into Customers (firstName) values (?)", "Anne");

    testDB.stop();
    return insertCount;
}

function testArrayOutParameters() returns (any, any, any, any, any, any) {
    endpoint jdbc:Client testDB {
        url:"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
    };

    string firstName;
    sql:Parameter para1 = {sqlType:sql:TYPE_ARRAY, direction:sql:DIRECTION_OUT};
    sql:Parameter para2 = {sqlType:sql:TYPE_ARRAY, direction:sql:DIRECTION_OUT};
    sql:Parameter para3 = {sqlType:sql:TYPE_ARRAY, direction:sql:DIRECTION_OUT};
    sql:Parameter para4 = {sqlType:sql:TYPE_ARRAY, direction:sql:DIRECTION_OUT};
    sql:Parameter para5 = {sqlType:sql:TYPE_ARRAY, direction:sql:DIRECTION_OUT};
    sql:Parameter para6 = {sqlType:sql:TYPE_ARRAY, direction:sql:DIRECTION_OUT};
    _ = testDB->call("{call TestArrayOutParams(?,?,?,?,?,?)}", (), para1, para2, para3, para4, para5, para6);
    testDB.stop();
    return (para1.value, para2.value, para3.value, para4.value, para5.value, para6.value);
}

function testArrayInOutParameters() returns (any, any, any, any, any, any, any) {
    endpoint jdbc:Client testDB {
        url:"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
    };

    sql:Parameter para1 = {sqlType:sql:TYPE_INTEGER, value:3};
    sql:Parameter para2 = {sqlType:sql:TYPE_INTEGER, direction:sql:DIRECTION_OUT};
    sql:Parameter para3 = {sqlType:sql:TYPE_ARRAY, value:"10,20,30", direction:sql:DIRECTION_INOUT};
    sql:Parameter para4 = {sqlType:sql:TYPE_ARRAY, value:"10000000, 20000000, 30000000", direction:sql:DIRECTION_INOUT};
    sql:Parameter para5 = {sqlType:sql:TYPE_ARRAY, value:"2454.23, 55594.49, 87964.123", direction:sql:DIRECTION_INOUT};
    sql:Parameter para6 = {sqlType:sql:TYPE_ARRAY, value:"2454.23, 55594.49, 87964.123", direction:sql:DIRECTION_INOUT};
    sql:Parameter para7 = {sqlType:sql:TYPE_ARRAY, value:"FALSE, FALSE, TRUE", direction:sql:DIRECTION_INOUT};
    sql:Parameter para8 = {sqlType:sql:TYPE_ARRAY, value:"Hello,Ballerina,Lang", direction:sql:DIRECTION_INOUT};

    _ = testDB->call("{call TestArrayInOutParams(?,?,?,?,?,?,?,?)}", (),
        para1, para2, para3, para4, para5, para6, para7, para8);
    testDB.stop();
    return (para2.value, para3.value, para4.value, para5.value, para6.value, para7.value, para8.value);
}

function testBatchUpdate() returns (int[]) {
    endpoint jdbc:Client testDB {
        url:"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
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

    int[] updateCount = check testDB->batchUpdate("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                                     values (?,?,?,?,?)", parameters1, parameters2);
    testDB.stop();
    return updateCount;
}

function testBatchUpdateWithFailure() returns (int[], int) {
    endpoint jdbc:Client testDB {
        url:"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
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

    int count;

    int[] updateCount = check testDB->batchUpdate("Insert into Customers (customerId, firstName,lastName,registrationID,creditLimit,
        country) values (?,?,?,?,?,?)", parameters1, parameters2, parameters3, parameters4);
    table dt = check testDB->select("SELECT count(*) as countval from Customers where customerId in (111,222,333)",
        ResultCount);

    while (dt.hasNext()) {
        var rs = check <ResultCount>dt.getNext();
        count = rs.COUNTVAL;
    }

    testDB.stop();
    return (updateCount, count);
}

function testBatchUpdateWithNullParam() returns (int[]) {
    endpoint jdbc:Client testDB {
        url:"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
    };

    int[] updateCount = check testDB->batchUpdate("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                                     values ('Alex','Smith',20,3400.5,'Colombo')");

    testDB.stop();
    return updateCount;
}

function testDateTimeInParameters() returns (int[]) {
    endpoint jdbc:Client testDB {
        url:"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
    };

    string stmt = "Insert into DateTimeTypes(row_id,date_type,time_type,datetime_type,timestamp_type) values (?,?,?,?,?)";
    int[] returnValues = [];
    sql:Parameter para1 = {sqlType:sql:TYPE_INTEGER, value:100};
    sql:Parameter para2 = {sqlType:sql:TYPE_DATE, value:"2017-01-30-08:01"};
    sql:Parameter para3 = {sqlType:sql:TYPE_TIME, value:"13:27:01.999999+08:33"};
    sql:Parameter para4 = {sqlType:sql:TYPE_TIMESTAMP, value:"2017-01-30T13:27:01.999-08:00"};
    sql:Parameter para5 = {sqlType:sql:TYPE_DATETIME, value:"2017-01-30T13:27:01.999999Z"};

    int insertCount1 = check testDB->update(stmt, para1, para2, para3, para4, para5);

    returnValues[0] = insertCount1;

    para1 = {sqlType:sql:TYPE_INTEGER, value:200};
    para2 = {sqlType:sql:TYPE_DATE, value:"-2017-01-30Z"};
    para3 = {sqlType:sql:TYPE_TIME, value:"13:27:01+08:33"};
    para4 = {sqlType:sql:TYPE_TIMESTAMP, value:"2017-01-30T13:27:01.999"};
    para5 = {sqlType:sql:TYPE_DATETIME, value:"-2017-01-30T13:27:01.999999-08:30"};

    int insertCount2 = check testDB->update(stmt, para1, para2, para3, para4, para5);

    returnValues[1] = insertCount2;

    time:Time timeNow = time:currentTime();
    para1 = {sqlType:sql:TYPE_INTEGER, value:300};
    para2 = {sqlType:sql:TYPE_DATE, value:timeNow};
    para3 = {sqlType:sql:TYPE_TIME, value:timeNow};
    para4 = {sqlType:sql:TYPE_TIMESTAMP, value:timeNow};
    para5 = {sqlType:sql:TYPE_DATETIME, value:timeNow};

    int insertCount3 = check testDB->update(stmt, para1, para2, para3, para4, para5);

    returnValues[2] = insertCount3;

    testDB.stop();
    return returnValues;
}

function testDateTimeNullInValues() returns (string) {
    endpoint jdbc:Client testDB {
        url:"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
    };

    sql:Parameter para0 = {sqlType:sql:TYPE_INTEGER, value:33};
    sql:Parameter para1 = {sqlType:sql:TYPE_DATE, value:()};
    sql:Parameter para2 = {sqlType:sql:TYPE_TIME, value:()};
    sql:Parameter para3 = {sqlType:sql:TYPE_TIMESTAMP, value:()};
    sql:Parameter para4 = {sqlType:sql:TYPE_DATETIME, value:()};
    sql:Parameter[] parameters = [para0, para1, para2, para3, para4];

    _ = testDB->update("Insert into DateTimeTypes
        (row_id, date_type, time_type, timestamp_type, datetime_type) values (?,?,?,?,?)",
        para0, para1, para2, para3, para4);

    table dt = check testDB->select("SELECT date_type, time_type, timestamp_type, datetime_type
                from DateTimeTypes where row_id = 33", ResultDates);

    string data;

    var j = check <json>dt;
    data = io:sprintf("%j", j);

    testDB.stop();
    return data;
}

function testDateTimeNullOutValues() returns (int) {
    endpoint jdbc:Client testDB {
        url:"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
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

    _ = testDB->call("{call TestDateTimeOutParams(?,?,?,?,?,?,?,?,?)}", (),
        para1, para2, para3, para4, para5, para6, para7, para8, para9);

    table dt = check testDB->select("SELECT count(*) as countval from DateTimeTypes where row_id = 123", ResultCount);

    int count;
    while (dt.hasNext()) {
        ResultCount rs = check <ResultCount>dt.getNext();
        count = rs.COUNTVAL;
    }
    testDB.stop();
    return count;
}

function testDateTimeNullInOutValues() returns (any, any, any, any) {
    endpoint jdbc:Client testDB {
        url:"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
    };

    sql:Parameter para1 = {sqlType:sql:TYPE_INTEGER, value:124};
    sql:Parameter para2 = {sqlType:sql:TYPE_DATE, value:null, direction:sql:DIRECTION_INOUT};
    sql:Parameter para3 = {sqlType:sql:TYPE_TIME, value:null, direction:sql:DIRECTION_INOUT};
    sql:Parameter para4 = {sqlType:sql:TYPE_TIMESTAMP, value:null, direction:sql:DIRECTION_INOUT};
    sql:Parameter para5 = {sqlType:sql:TYPE_DATETIME, value:null, direction:sql:DIRECTION_INOUT};

    _ = testDB->call("{call TestDateINOUTParams(?,?,?,?,?)}", (), para1, para2, para3, para4, para5);
    testDB.stop();
    return (para2.value, para3.value, para4.value, para5.value);
}

function testDateTimeOutParams(int time, int date, int timestamp) returns (int) {
    endpoint jdbc:Client testDB {
        url:"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
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

    _ = testDB->call("{call TestDateTimeOutParams(?,?,?,?,?,?,?,?,?)}", (),
        para1, para2, para3, para4, para5, para6, para7, para8, para9);

    table dt = check testDB->select("SELECT count(*) as countval from DateTimeTypes where row_id = 10", ResultCount);

    int count;
    while (dt.hasNext()) {
        ResultCount rs = check <ResultCount>dt.getNext();
        count = rs.COUNTVAL;
    }
    testDB.stop();
    return count;
}

function testStructOutParameters() returns (any) {
    endpoint jdbc:Client testDB {
        url:"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
    };

    sql:Parameter para1 = {sqlType:sql:TYPE_STRUCT, direction:sql:DIRECTION_OUT};
    _ = testDB->call("{call TestStructOut(?)}", (), para1);
    testDB.stop();
    return para1.value;
}

function testComplexTypeRetrieval() returns (string, string, string, string) {
    endpoint jdbc:Client testDB {
        url:"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
    };

    string s1;
    string s2;
    string s3;
    string s4;

    table dt = check testDB->select("SELECT * from DataTypeTable where row_id = 1", ());
    var x1 = check <xml>dt;
    s1 = io:sprintf("%l", x1);

    dt = check testDB->select("SELECT * from DateTimeTypes where row_id = 1", ());
    var x2 = check <xml>dt;
    s2 = io:sprintf("%l", x2);

    dt = check testDB->select("SELECT * from DataTypeTable where row_id = 1", ());
    var j = check <json>dt;
    s3 = io:sprintf("%j", j);

    dt = check testDB->select("SELECT * from DateTimeTypes where row_id = 1", ());
    j = check <json>dt;
    s4 = io:sprintf("%j", j);

    testDB.stop();
    return (s1, s2, s3, s4);
}

function testSelectLoadToMemory() returns (Employee[], Employee[], Employee[]) {
    endpoint sql:Client testDB {
        url:"hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
    };

    var temp = testDB->select("SELECT * from employeeItr", Employee, loadToMemory = true);
    table dt = check temp;

    Employee[] employeeArray1;
    Employee[] employeeArray2;
    Employee[] employeeArray3;
    int i = 0;
    while (dt.hasNext()) {
        var rs = check <Employee>dt.getNext();
        Employee e = {id:rs.id, name:rs.name, address:rs.address};
        employeeArray1[i] = e;
        i++;
    }

    i = 0;
    while (dt.hasNext()) {
        var rs = check <Employee>dt.getNext();
        Employee e = {id:rs.id, name:rs.name, address:rs.address};
        employeeArray2[i] = e;
        i++;
    }

    i = 0;
    while (dt.hasNext()) {
        var rs = check <Employee>dt.getNext();
        Employee e = {id:rs.id, name:rs.name, address:rs.address};
        employeeArray3[i] = e;
        i++;
    }

    _ = testDB->close();
    return (employeeArray1, employeeArray2, employeeArray3);
}

function testLoadToMemorySelectAfterTableClose() returns (Employee[], Employee[], error) {
    endpoint sql:Client testDB {
        url:"hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
    };

    var temp = testDB->select("SELECT * from employeeItr", Employee, loadToMemory = true);
    table dt = check temp;

    Employee[] employeeArray1;
    Employee[] employeeArray2;
    Employee[] employeeArray3;
    int i = 0;
    while (dt.hasNext()) {
        var rs = check <Employee>dt.getNext();
        Employee e = {id:rs.id, name:rs.name, address:rs.address};
        employeeArray1[i] = e;
        i++;
    }
    i = 0;
    while (dt.hasNext()) {
        var rs = check <Employee>dt.getNext();
        Employee e = {id:rs.id, name:rs.name, address:rs.address};
        employeeArray2[i] = e;
        i++;
    }
    dt.close();
    i = 0;
    error e;
    try {
        while (dt.hasNext()) {
            var rs = check <Employee>dt.getNext();
            Employee e = {id:rs.id, name:rs.name, address:rs.address};
            employeeArray3[i] = e;
            i++;
        }}
    catch (error err) {
        e = err;
    }
    _ = testDB->close();
    return (employeeArray1, employeeArray2, e);
}

function testCloseConnectionPool() returns (int) {
    endpoint jdbc:Client testDB {
        url:"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
    };

    table dt = check testDB->select("SELECT COUNT(*) as countVal FROM INFORMATION_SCHEMA.SYSTEM_SESSIONS", ResultCount);

    int count;
    while (dt.hasNext()) {
        ResultCount rs = check <ResultCount>dt.getNext();
        count = rs.COUNTVAL;
    }
    testDB.stop();
    return count;
}
