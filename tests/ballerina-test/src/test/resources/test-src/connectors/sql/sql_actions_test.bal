import ballerina/sql;
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

function testInsertTableData() returns (int) {
    endpoint sql:Client testDB {
        url:"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
    };

    var insertCount = check testDB->update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                                         values ('James', 'Clerk', 2, 5000.75, 'USA')");
    _ = testDB->close();
    return insertCount;
}

function testCreateTable() returns (int) {
    endpoint sql:Client testDB {
        url:"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
    };

    int returnValue = check testDB->update("CREATE TABLE IF NOT EXISTS Students(studentID int, LastName varchar(255))");
    _ = testDB->close();
    return returnValue;
}

function testUpdateTableData() returns (int) {
    endpoint sql:Client testDB {
        url:"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
    };

    var updateCount = check testDB->update("Update Customers set country = 'UK' where registrationID = 1");
    _ = testDB->close();
    return updateCount;
}

function testGeneratedKeyOnInsert() returns (string) {
    endpoint sql:Client testDB {
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

    _ = testDB->close();
    return returnVal;
}

function testGeneratedKeyWithColumn() returns (string) {
    endpoint sql:Client testDB {
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

    _ = testDB->close();
    return returnVal;
}

function testSelectData() returns (string) {
    endpoint sql:Client testDB {
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
    _ = testDB->close();
    return firstName;
}

function testSelectIntFloatData() returns (int, int, float, float) {
    endpoint sql:Client testDB {
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
    _ = testDB->close();
    return (int_type, long_type, float_type, double_type);
}

function testCallProcedure() returns (string) {
    endpoint sql:Client testDB {
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
    _ = testDB->close();
    return firstName;
}

function testCallProcedureWithResultSet() returns (string) {
    endpoint sql:Client testDB {
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
    _ = testDB->close();
    return firstName;
}

function testCallProcedureWithMultipleResultSets() returns (string, string, string) {
    endpoint sql:Client testDB {
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

    _ = testDB->close();
    return (firstName1, firstName2, lastName);
}

function testQueryParameters() returns (string) {
    endpoint sql:Client testDB {
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
    _ = testDB->close();
    return firstName;
}

function testQueryParameters2() returns (string) {
    endpoint sql:Client testDB {
        url:"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
    };

    table dt = check testDB->select("SELECT  FirstName from Customers where registrationID = ?", ResultCustomers, (sql:
        TYPE_INTEGER, 1));

    string firstName;

    while (dt.hasNext()) {
        ResultCustomers rs = check <ResultCustomers>dt.getNext();
        firstName = rs.FIRSTNAME;
    }
    _ = testDB->close();
    return firstName;
}

function testInsertTableDataWithParameters() returns (int) {
    endpoint sql:Client testDB {
        url:"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
    };

    string s1 = "Anne";
    sql:Parameter para1 = (sql:TYPE_VARCHAR, s1, sql:DIRECTION_IN);
    sql:Parameter para2 = (sql:TYPE_VARCHAR, "James", sql:DIRECTION_IN);
    sql:Parameter para3 = (sql:TYPE_INTEGER, 3, sql:DIRECTION_IN);
    sql:Parameter para4 = (sql:TYPE_DOUBLE, 5000.75, sql:DIRECTION_IN);
    sql:Parameter para5 = (sql:TYPE_VARCHAR, "UK", sql:DIRECTION_IN);

    int insertCount = check testDB->update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                                     values (?,?,?,?,?)", para1, para2, para3, para4, para5);
    _ = testDB->close();
    return insertCount;
}

function testInsertTableDataWithParameters2() returns (int) {
    endpoint sql:Client testDB {
        url:"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
    };

    string s1 = "Anne";
    int insertCount = check testDB->update("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                                     values (?,?,?,?,?)", s1, "James", 3, 5000.75, "UK");

    _ = testDB->close();
    return insertCount;
}


function testArrayofQueryParameters() returns (string) {
    endpoint sql:Client testDB {
        url:"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
    };

    int[] intDataArray = [1, 4343];
    string[] stringDataArray = ["A", "B"];
    float[] doubleArray = [233.4, 433.4];
    sql:Parameter para0 = (sql:TYPE_VARCHAR, "Johhhn");
    sql:Parameter para1 = (sql:TYPE_INTEGER, intDataArray);
    sql:Parameter para2 = (sql:TYPE_VARCHAR, stringDataArray);
    sql:Parameter para3 = (sql:TYPE_DOUBLE, doubleArray);

    table dt = check testDB->select("SELECT  FirstName from Customers where FirstName = ? or lastName = 'A' or
        lastName = '\"BB\"' or registrationID in(?) or lastName in(?) or creditLimit in(?)", ResultCustomers,
        para0, para1, para2, para3);

    string firstName;
    while (dt.hasNext()) {
        ResultCustomers rs = check <ResultCustomers>dt.getNext();
        firstName = rs.FIRSTNAME;
    }
    _ = testDB->close();
    return firstName;
}

function testBoolArrayofQueryParameters() returns (int) {
    endpoint sql:Client testDB {
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

    sql:Parameter para0 = (sql:TYPE_INTEGER, 1);
    sql:Parameter para1 = (sql:TYPE_BOOLEAN, boolDataArray);
    sql:Parameter para2 = (sql:TYPE_BLOB, blobDataArray);

    table dt = check testDB->select("SELECT  int_type from DataTypeTable where row_id = ? and boolean_type in(?) and
        blob_type in (?)", ResultIntType, para0, para1, para2);

    int value;
    while (dt.hasNext()) {
        ResultIntType rs = check <ResultIntType>dt.getNext();
        value = rs.INT_TYPE;
    }
    _ = testDB->close();
    return value;
}

function testArrayInParameters() returns (int, map, map, map, map, map, map) {
    endpoint sql:Client testDB {
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
    sql:Parameter para1 = (sql:TYPE_INTEGER, 2);
    sql:Parameter para2 = (sql:TYPE_ARRAY, intArray);
    sql:Parameter para3 = (sql:TYPE_ARRAY, longArray);
    sql:Parameter para4 = (sql:TYPE_ARRAY, floatArray);
    sql:Parameter para5 = (sql:TYPE_ARRAY, doubleArray);
    sql:Parameter para6 = (sql:TYPE_ARRAY, boolArray);
    sql:Parameter para7 = (sql:TYPE_ARRAY, stringArray);

    int insertCount;
    map int_arr;
    map long_arr;
    map double_arr;
    map string_arr;
    map boolean_arr;
    map float_arr;

    insertCount = check testDB->update("INSERT INTO ArrayTypes (row_id, int_array, long_array,
        float_array, double_array, boolean_array, string_array) values (?,?,?,?,?,?,?)", para1, para2, para3, para4,
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
    _ = testDB->close();
    return (insertCount, int_arr, long_arr, double_arr, string_arr, boolean_arr, float_arr);
}

function testOutParameters() returns (any, any, any, any, any, any, any, any, any, any, any, any, any, any) {
    endpoint sql:Client testDB {
        url:"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
    };

    sql:Parameter paraID = (sql:TYPE_INTEGER, "1");
    sql:CallParam paraInt = {sqlType:sql:TYPE_INTEGER, direction:sql:DIRECTION_OUT};
    sql:CallParam paraLong = {sqlType:sql:TYPE_BIGINT, direction:sql:DIRECTION_OUT};
    sql:CallParam paraFloat = {sqlType:sql:TYPE_FLOAT, direction:sql:DIRECTION_OUT};
    sql:CallParam paraDouble = {sqlType:sql:TYPE_DOUBLE, direction:sql:DIRECTION_OUT};
    sql:CallParam paraBool = {sqlType:sql:TYPE_BOOLEAN, direction:sql:DIRECTION_OUT};
    sql:CallParam paraString = {sqlType:sql:TYPE_VARCHAR, direction:sql:DIRECTION_OUT};
    sql:CallParam paraNumeric = {sqlType:sql:TYPE_NUMERIC, direction:sql:DIRECTION_OUT};
    sql:CallParam paraDecimal = {sqlType:sql:TYPE_DECIMAL, direction:sql:DIRECTION_OUT};
    sql:CallParam paraReal = {sqlType:sql:TYPE_REAL, direction:sql:DIRECTION_OUT};
    sql:CallParam paraTinyInt = {sqlType:sql:TYPE_TINYINT, direction:sql:DIRECTION_OUT};
    sql:CallParam paraSmallInt = {sqlType:sql:TYPE_SMALLINT, direction:sql:DIRECTION_OUT};
    sql:CallParam paraClob = {sqlType:sql:TYPE_CLOB, direction:sql:DIRECTION_OUT};
    sql:CallParam paraBlob = {sqlType:sql:TYPE_BLOB, direction:sql:DIRECTION_OUT};
    sql:CallParam paraBinary = {sqlType:sql:TYPE_BINARY, direction:sql:DIRECTION_OUT};

    _ = testDB->call("{call TestOutParams(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}", (),
        paraID, paraInt, paraLong, paraFloat, paraDouble, paraBool, paraString, paraNumeric,
        paraDecimal, paraReal, paraTinyInt, paraSmallInt, paraClob, paraBlob, paraBinary);

    _ = testDB->close();

    return (paraInt.value, paraLong.value, paraFloat.value, paraDouble.value, paraBool.value, paraString.value,
    paraNumeric.value, paraDecimal.value, paraReal.value, paraTinyInt.value, paraSmallInt.value, paraClob.value,
    paraBlob.value, paraBinary.value);
}

function testNullOutParameters() returns (any, any, any, any, any, any, any, any, any, any, any, any, any, any) {
    endpoint sql:Client testDB {
        url:"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
    };

    sql:Parameter paraID = (sql:TYPE_INTEGER, "2");
    sql:CallParam paraInt = {sqlType:sql:TYPE_INTEGER, direction:sql:DIRECTION_OUT};
    sql:CallParam paraLong = {sqlType:sql:TYPE_BIGINT, direction:sql:DIRECTION_OUT};
    sql:CallParam paraFloat = {sqlType:sql:TYPE_FLOAT, direction:sql:DIRECTION_OUT};
    sql:CallParam paraDouble = {sqlType:sql:TYPE_DOUBLE, direction:sql:DIRECTION_OUT};
    sql:CallParam paraBool = {sqlType:sql:TYPE_BOOLEAN, direction:sql:DIRECTION_OUT};
    sql:CallParam paraString = {sqlType:sql:TYPE_VARCHAR, direction:sql:DIRECTION_OUT};
    sql:CallParam paraNumeric = {sqlType:sql:TYPE_NUMERIC, direction:sql:DIRECTION_OUT};
    sql:CallParam paraDecimal = {sqlType:sql:TYPE_DECIMAL, direction:sql:DIRECTION_OUT};
    sql:CallParam paraReal = {sqlType:sql:TYPE_REAL, direction:sql:DIRECTION_OUT};
    sql:CallParam paraTinyInt = {sqlType:sql:TYPE_TINYINT, direction:sql:DIRECTION_OUT};
    sql:CallParam paraSmallInt = {sqlType:sql:TYPE_SMALLINT, direction:sql:DIRECTION_OUT};
    sql:CallParam paraClob = {sqlType:sql:TYPE_CLOB, direction:sql:DIRECTION_OUT};
    sql:CallParam paraBlob = {sqlType:sql:TYPE_BLOB, direction:sql:DIRECTION_OUT};
    sql:CallParam paraBinary = {sqlType:sql:TYPE_BINARY, direction:sql:DIRECTION_OUT};

    _ = testDB->call("{call TestOutParams(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}", (),
        paraID, paraInt, paraLong, paraFloat, paraDouble, paraBool, paraString, paraNumeric,
        paraDecimal, paraReal, paraTinyInt, paraSmallInt, paraClob, paraBlob, paraBinary);
    _ = testDB->close();
    return (paraInt.value, paraLong.value, paraFloat.value, paraDouble.value, paraBool.value, paraString.value,
    paraNumeric.value, paraDecimal.value, paraReal.value, paraTinyInt.value, paraSmallInt.value, paraClob.value,
    paraBlob.value, paraBinary.value);
}

function testINParameters() returns (int) {
    endpoint sql:Client testDB {
        url:"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
    };

    sql:Parameter paraID = (sql:TYPE_INTEGER, 3);
    sql:Parameter paraInt = (sql:TYPE_INTEGER, 1);
    sql:Parameter paraLong = (sql:TYPE_BIGINT, "9223372036854774807");
    sql:Parameter paraFloat = (sql:TYPE_FLOAT, 123.34);
    sql:Parameter paraDouble = (sql:TYPE_DOUBLE, 2139095039);
    sql:Parameter paraBool = (sql:TYPE_BOOLEAN, true);
    sql:Parameter paraString = (sql:TYPE_VARCHAR, "Hello");
    sql:Parameter paraNumeric = (sql:TYPE_NUMERIC, 1234.567);
    sql:Parameter paraDecimal = (sql:TYPE_DECIMAL, 1234.567);
    sql:Parameter paraReal = (sql:TYPE_REAL, 1234.567);
    sql:Parameter paraTinyInt = (sql:TYPE_TINYINT, 1);
    sql:Parameter paraSmallInt = (sql:TYPE_SMALLINT, 5555);
    sql:Parameter paraClob = (sql:TYPE_CLOB, "very long text");
    sql:Parameter paraBlob = (sql:TYPE_BLOB, "YmxvYiBkYXRh");
    sql:Parameter paraBinary = (sql:TYPE_BINARY, "d3NvMiBiYWxsZXJpbmEgYmluYXJ5IHRlc3Qu");

    int insertCount = check testDB->update("INSERT INTO DataTypeTable (row_id,int_type, long_type,
            float_type, double_type, boolean_type, string_type, numeric_type, decimal_type, real_type, tinyint_type,
            smallint_type, clob_type, blob_type, binary_type) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
        paraID, paraInt, paraLong, paraFloat, paraDouble, paraBool, paraString, paraNumeric,
        paraDecimal, paraReal, paraTinyInt, paraSmallInt, paraClob, paraBlob, paraBinary);
    _ = testDB->close();
    return insertCount;
}

function testNullINParameterValues() returns (int) {
    endpoint sql:Client testDB {
        url:"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
    };

    sql:Parameter paraID = (sql:TYPE_INTEGER, 4);
    sql:Parameter paraInt = (sql:TYPE_INTEGER, ());
    sql:Parameter paraLong = (sql:TYPE_BIGINT, ());
    sql:Parameter paraFloat = (sql:TYPE_FLOAT, ());
    sql:Parameter paraDouble = (sql:TYPE_DOUBLE, ());
    sql:Parameter paraBool = (sql:TYPE_BOOLEAN, ());
    sql:Parameter paraString = (sql:TYPE_VARCHAR, ());
    sql:Parameter paraNumeric = (sql:TYPE_NUMERIC, ());
    sql:Parameter paraDecimal = (sql:TYPE_DECIMAL, ());
    sql:Parameter paraReal = (sql:TYPE_REAL, ());
    sql:Parameter paraTinyInt = (sql:TYPE_TINYINT, ());
    sql:Parameter paraSmallInt = (sql:TYPE_SMALLINT, ());
    sql:Parameter paraClob = (sql:TYPE_CLOB, ());
    sql:Parameter paraBlob = (sql:TYPE_BLOB, ());
    sql:Parameter paraBinary = (sql:TYPE_BINARY, ());

    int insertCount = check testDB->update("INSERT INTO DataTypeTable (row_id, int_type, long_type,
            float_type, double_type, boolean_type, string_type, numeric_type, decimal_type, real_type, tinyint_type,
            smallint_type, clob_type, blob_type, binary_type) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
        paraID, paraInt, paraLong, paraFloat, paraDouble, paraBool, paraString, paraNumeric,
        paraDecimal, paraReal, paraTinyInt, paraSmallInt, paraClob, paraBlob, paraBinary);
    _ = testDB->close();
    return insertCount;
}

function testINOutParameters() returns (any, any, any, any, any, any, any, any, any, any, any, any, any, any) {
    endpoint sql:Client testDB {
        url:"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
    };

    sql:Parameter paraID = (sql:TYPE_INTEGER, 5);
    sql:CallParam paraInt = {sqlType:sql:TYPE_INTEGER, value:10, direction:sql:DIRECTION_INOUT};
    sql:CallParam paraLong = {sqlType:sql:TYPE_BIGINT, value:"9223372036854774807", direction:sql:DIRECTION_INOUT};
    sql:CallParam paraFloat = {sqlType:sql:TYPE_FLOAT, value:123.34, direction:sql:DIRECTION_INOUT};
    sql:CallParam paraDouble = {sqlType:sql:TYPE_DOUBLE, value:2139095039, direction:sql:DIRECTION_INOUT};
    sql:CallParam paraBool = {sqlType:sql:TYPE_BOOLEAN, value:true, direction:sql:DIRECTION_INOUT};
    sql:CallParam paraString = {sqlType:sql:TYPE_VARCHAR, value:"Hello", direction:sql:DIRECTION_INOUT};
    sql:CallParam paraNumeric = {sqlType:sql:TYPE_NUMERIC, value:1234.567, direction:sql:DIRECTION_INOUT};
    sql:CallParam paraDecimal = {sqlType:sql:TYPE_DECIMAL, value:1234.567, direction:sql:DIRECTION_INOUT};
    sql:CallParam paraReal = {sqlType:sql:TYPE_REAL, value:1234.567, direction:sql:DIRECTION_INOUT};
    sql:CallParam paraTinyInt = {sqlType:sql:TYPE_TINYINT, value:1, direction:sql:DIRECTION_INOUT};
    sql:CallParam paraSmallInt = {sqlType:sql:TYPE_SMALLINT, value:5555, direction:sql:DIRECTION_INOUT};
    sql:CallParam paraClob = {sqlType:sql:TYPE_CLOB, value:"very long text", direction:sql:DIRECTION_INOUT};
    sql:CallParam paraBlob = {sqlType:sql:TYPE_BLOB, value:"YmxvYiBkYXRh", direction:sql:DIRECTION_INOUT};
    sql:CallParam paraBinary = {sqlType:sql:TYPE_BINARY, value:"d3NvMiBiYWxsZXJpbmEgYmluYXJ5IHRlc3Qu", direction:sql:
    DIRECTION_INOUT};

    _ = testDB->call("{call TestINOUTParams(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}", (),
        paraID, paraInt, paraLong, paraFloat, paraDouble, paraBool, paraString, paraNumeric,
        paraDecimal, paraReal, paraTinyInt, paraSmallInt, paraClob, paraBlob, paraBinary);
    _ = testDB->close();
    return (paraInt.value, paraLong.value, paraFloat.value, paraDouble.value, paraBool.value, paraString.value,
    paraNumeric.value, paraDecimal.value, paraReal.value, paraTinyInt.value, paraSmallInt.value, paraClob.value,
    paraBlob.value, paraBinary.value);
}

function testNullINOutParameters() returns (any, any, any, any, any, any, any, any, any, any, any, any, any, any) {
    endpoint sql:Client testDB {
        url:"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
    };

    sql:Parameter paraID = (sql:TYPE_INTEGER, "6");
    sql:CallParam paraInt = {sqlType:sql:TYPE_INTEGER, direction:sql:DIRECTION_INOUT};
    sql:CallParam paraLong = {sqlType:sql:TYPE_BIGINT, direction:sql:DIRECTION_INOUT};
    sql:CallParam paraFloat = {sqlType:sql:TYPE_FLOAT, direction:sql:DIRECTION_INOUT};
    sql:CallParam paraDouble = {sqlType:sql:TYPE_DOUBLE, direction:sql:DIRECTION_INOUT};
    sql:CallParam paraBool = {sqlType:sql:TYPE_BOOLEAN, direction:sql:DIRECTION_INOUT};
    sql:CallParam paraString = {sqlType:sql:TYPE_VARCHAR, direction:sql:DIRECTION_INOUT};
    sql:CallParam paraNumeric = {sqlType:sql:TYPE_NUMERIC, direction:sql:DIRECTION_INOUT};
    sql:CallParam paraDecimal = {sqlType:sql:TYPE_DECIMAL, direction:sql:DIRECTION_INOUT};
    sql:CallParam paraReal = {sqlType:sql:TYPE_REAL, direction:sql:DIRECTION_INOUT};
    sql:CallParam paraTinyInt = {sqlType:sql:TYPE_TINYINT, direction:sql:DIRECTION_INOUT};
    sql:CallParam paraSmallInt = {sqlType:sql:TYPE_SMALLINT, direction:sql:DIRECTION_INOUT};
    sql:CallParam paraClob = {sqlType:sql:TYPE_CLOB, direction:sql:DIRECTION_INOUT};
    sql:CallParam paraBlob = {sqlType:sql:TYPE_BLOB, direction:sql:DIRECTION_INOUT};
    sql:CallParam paraBinary = {sqlType:sql:TYPE_BINARY, direction:sql:DIRECTION_INOUT};

    _ = testDB->call("{call TestINOUTParams(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}", (),
        paraID, paraInt, paraLong, paraFloat, paraDouble, paraBool, paraString, paraNumeric,
        paraDecimal, paraReal, paraTinyInt, paraSmallInt, paraClob, paraBlob, paraBinary);
    _ = testDB->close();
    return (paraInt.value, paraLong.value, paraFloat.value, paraDouble.value, paraBool.value, paraString.value,
    paraNumeric.value, paraDecimal.value, paraReal.value, paraTinyInt.value, paraSmallInt.value, paraClob.value,
    paraBlob.value, paraBinary.value);
}

function testEmptySQLType() returns (int) {
    endpoint sql:Client testDB {
        url:"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
    };

    int insertCount = check testDB->update("Insert into Customers (firstName) values (?)", "Anne");

    _ = testDB->close();
    return insertCount;
}

function testArrayOutParameters() returns (any, any, any, any, any, any) {
    endpoint sql:Client testDB {
        url:"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
    };

    string firstName;
    sql:CallParam para1 = {sqlType:sql:TYPE_ARRAY, direction:sql:DIRECTION_OUT};
    sql:CallParam para2 = {sqlType:sql:TYPE_ARRAY, direction:sql:DIRECTION_OUT};
    sql:CallParam para3 = {sqlType:sql:TYPE_ARRAY, direction:sql:DIRECTION_OUT};
    sql:CallParam para4 = {sqlType:sql:TYPE_ARRAY, direction:sql:DIRECTION_OUT};
    sql:CallParam para5 = {sqlType:sql:TYPE_ARRAY, direction:sql:DIRECTION_OUT};
    sql:CallParam para6 = {sqlType:sql:TYPE_ARRAY, direction:sql:DIRECTION_OUT};
    _ = testDB->call("{call TestArrayOutParams(?,?,?,?,?,?)}", (), para1, para2, para3, para4, para5, para6);
    _ = testDB->close();
    return (para1.value, para2.value, para3.value, para4.value, para5.value, para6.value);
}

function testArrayInOutParameters() returns (any, any, any, any, any, any, any) {
    endpoint sql:Client testDB {
        url:"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
    };

    sql:Parameter para1 = (sql:TYPE_INTEGER, 3);
    sql:CallParam para2 = {sqlType:sql:TYPE_INTEGER, direction:sql:DIRECTION_OUT};
    sql:CallParam para3 = {sqlType:sql:TYPE_ARRAY, value:"10,20,30", direction:sql:DIRECTION_INOUT};
    sql:CallParam para4 = {sqlType:sql:TYPE_ARRAY, value:"10000000, 20000000, 30000000", direction:sql:DIRECTION_INOUT};
    sql:CallParam para5 = {sqlType:sql:TYPE_ARRAY, value:"2454.23, 55594.49, 87964.123", direction:sql:DIRECTION_INOUT};
    sql:CallParam para6 = {sqlType:sql:TYPE_ARRAY, value:"2454.23, 55594.49, 87964.123", direction:sql:DIRECTION_INOUT};
    sql:CallParam para7 = {sqlType:sql:TYPE_ARRAY, value:"FALSE, FALSE, TRUE", direction:sql:DIRECTION_INOUT};
    sql:CallParam para8 = {sqlType:sql:TYPE_ARRAY, value:"Hello,Ballerina,Lang", direction:sql:DIRECTION_INOUT};

    _ = testDB->call("{call TestArrayInOutParams(?,?,?,?,?,?,?,?)}", (),
        para1, para2, para3, para4, para5, para6, para7, para8);
    _ = testDB->close();
    return (para2.value, para3.value, para4.value, para5.value, para6.value, para7.value, para8.value);
}

function testBatchUpdate() returns (int[]) {
    endpoint sql:Client testDB {
        url:"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
    };

    //Batch 1
    sql:Parameter para1 = (sql:TYPE_VARCHAR, "Alex");
    sql:Parameter para2 = (sql:TYPE_VARCHAR, "Smith");
    sql:Parameter para3 = (sql:TYPE_INTEGER, 20);
    sql:Parameter para4 = (sql:TYPE_DOUBLE, 3400.5);
    sql:Parameter para5 = (sql:TYPE_VARCHAR, "Colombo");
    sql:Parameter[] parameters1 = [para1, para2, para3, para4, para5];

    //Batch 2
    para1 = (sql:TYPE_VARCHAR, "Alex");
    para2 = (sql:TYPE_VARCHAR, "Smith");
    para3 = (sql:TYPE_INTEGER, 20);
    para4 = (sql:TYPE_DOUBLE, 3400.5);
    para5 = (sql:TYPE_VARCHAR, "Colombo");
    sql:Parameter[] parameters2 = [para1, para2, para3, para4, para5];

    int[] updateCount = check testDB->batchUpdate("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                                     values (?,?,?,?,?)", parameters1, parameters2);
    _ = testDB->close();
    return updateCount;
}

function testBatchUpdateWithFailure() returns (int[], int) {
    endpoint sql:Client testDB {
        url:"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
    };

    //Batch 1
    sql:Parameter para0 = (sql:TYPE_INTEGER, 111);
    sql:Parameter para1 = (sql:TYPE_VARCHAR, "Alex");
    sql:Parameter para2 = (sql:TYPE_VARCHAR, "Smith");
    sql:Parameter para3 = (sql:TYPE_INTEGER, 20);
    sql:Parameter para4 = (sql:TYPE_DOUBLE, 3400.5);
    sql:Parameter para5 = (sql:TYPE_VARCHAR, "Colombo");
    sql:Parameter[] parameters1 = [para0, para1, para2, para3, para4, para5];

    //Batch 2
    para0 = (sql:TYPE_INTEGER, 222);
    para1 = (sql:TYPE_VARCHAR, "Alex");
    para2 = (sql:TYPE_VARCHAR, "Smith");
    para3 = (sql:TYPE_INTEGER, 20);
    para4 = (sql:TYPE_DOUBLE, 3400.5);
    para5 = (sql:TYPE_VARCHAR, "Colombo");
    sql:Parameter[] parameters2 = [para0, para1, para2, para3, para4, para5];

    //Batch 3
    para0 = (sql:TYPE_INTEGER, 222);
    para1 = (sql:TYPE_VARCHAR, "Alex");
    para2 = (sql:TYPE_VARCHAR, "Smith");
    para3 = (sql:TYPE_INTEGER, 20);
    para4 = (sql:TYPE_DOUBLE, 3400.5);
    para5 = (sql:TYPE_VARCHAR, "Colombo");
    sql:Parameter[] parameters3 = [para0, para1, para2, para3, para4, para5];

    //Batch 4
    para0 = (sql:TYPE_INTEGER, 333);
    para1 = (sql:TYPE_VARCHAR, "Alex");
    para2 = (sql:TYPE_VARCHAR, "Smith");
    para3 = (sql:TYPE_INTEGER, 20);
    para4 = (sql:TYPE_DOUBLE, 3400.5);
    para5 = (sql:TYPE_VARCHAR, "Colombo");
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

    _ = testDB->close();
    return (updateCount, count);
}

function testBatchUpdateWithNullParam() returns (int[]) {
    endpoint sql:Client testDB {
        url:"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
    };

    int[] updateCount = check testDB->batchUpdate("Insert into Customers (firstName,lastName,registrationID,creditLimit,country)
                                     values ('Alex','Smith',20,3400.5,'Colombo')");

    _ = testDB->close();
    return updateCount;
}

function testDateTimeInParameters() returns (int[]) {
    endpoint sql:Client testDB {
        url:"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
    };

    string stmt = "Insert into DateTimeTypes(row_id,date_type,time_type,datetime_type,timestamp_type) values (?,?,?,?,?)";
    int[] returnValues = [];
    sql:Parameter para1 = (sql:TYPE_INTEGER, 100);
    sql:Parameter para2 = (sql:TYPE_DATE, "2017-01-30-08:01");
    sql:Parameter para3 = (sql:TYPE_TIME, "13:27:01.999999+08:33");
    sql:Parameter para4 = (sql:TYPE_TIMESTAMP, "2017-01-30T13:27:01.999-08:00");
    sql:Parameter para5 = (sql:TYPE_DATETIME, "2017-01-30T13:27:01.999999Z");

    int insertCount1 = check testDB->update(stmt, para1, para2, para3, para4, para5);

    returnValues[0] = insertCount1;

    para1 = (sql:TYPE_INTEGER, 200);
    para2 = (sql:TYPE_DATE, "-2017-01-30Z");
    para3 = (sql:TYPE_TIME, "13:27:01+08:33");
    para4 = (sql:TYPE_TIMESTAMP, "2017-01-30T13:27:01.999");
    para5 = (sql:TYPE_DATETIME, "-2017-01-30T13:27:01.999999-08:30");

    int insertCount2 = check testDB->update(stmt, para1, para2, para3, para4, para5);

    returnValues[1] = insertCount2;

    time:Time timeNow = time:currentTime();
    para1 = (sql:TYPE_INTEGER, 300);
    para2 = (sql:TYPE_DATE, timeNow);
    para3 = (sql:TYPE_TIME, timeNow);
    para4 = (sql:TYPE_TIMESTAMP, timeNow);
    para5 = (sql:TYPE_DATETIME, timeNow);

    int insertCount3 = check testDB->update(stmt, para1, para2, para3, para4, para5);

    returnValues[2] = insertCount3;

    _ = testDB->close();
    return returnValues;
}

function testDateTimeNullInValues() returns (string) {
    endpoint sql:Client testDB {
        url:"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
    };

    sql:Parameter para0 = (sql:TYPE_INTEGER, 33);
    sql:Parameter para1 = (sql:TYPE_DATE, ());
    sql:Parameter para2 = (sql:TYPE_TIME, ());
    sql:Parameter para3 = (sql:TYPE_TIMESTAMP, ());
    sql:Parameter para4 = (sql:TYPE_DATETIME, ());
    sql:Parameter[] parameters = [para0, para1, para2, para3, para4];

    _ = testDB->update("Insert into DateTimeTypes
        (row_id, date_type, time_type, timestamp_type, datetime_type) values (?,?,?,?,?)",
        para0, para1, para2, para3, para4);

    table dt = check testDB->select("SELECT date_type, time_type, timestamp_type, datetime_type
                from DateTimeTypes where row_id = 33", ResultDates);

    string data;

    var j = check <json>dt;
    data = io:sprintf("%j", [j]);

    _ = testDB->close();
    return data;
}

function testDateTimeNullOutValues() returns (int) {
    endpoint sql:Client testDB {
        url:"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
    };

    sql:Parameter para1 = (sql:TYPE_INTEGER, 123);
    sql:Parameter para2 = (sql:TYPE_DATE, ());
    sql:Parameter para3 = (sql:TYPE_TIME, ());
    sql:Parameter para4 = (sql:TYPE_TIMESTAMP, ());
    sql:Parameter para5 = (sql:TYPE_DATETIME, ());

    sql:CallParam para6 = {sqlType:sql:TYPE_DATE, direction:sql:DIRECTION_OUT};
    sql:CallParam para7 = {sqlType:sql:TYPE_TIME, direction:sql:DIRECTION_OUT};
    sql:CallParam para8 = {sqlType:sql:TYPE_TIMESTAMP, direction:sql:DIRECTION_OUT};
    sql:CallParam para9 = {sqlType:sql:TYPE_DATETIME, direction:sql:DIRECTION_OUT};

    _ = testDB->call("{call TestDateTimeOutParams(?,?,?,?,?,?,?,?,?)}", (),
        para1, para2, para3, para4, para5, para6, para7, para8, para9);

    table dt = check testDB->select("SELECT count(*) as countval from DateTimeTypes where row_id = 123", ResultCount);

    int count;
    while (dt.hasNext()) {
        ResultCount rs = check <ResultCount>dt.getNext();
        count = rs.COUNTVAL;
    }
    _ = testDB->close();
    return count;
}

function testDateTimeNullInOutValues() returns (any, any, any, any) {
    endpoint sql:Client testDB {
        url:"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
    };

    sql:Parameter para1 = (sql:TYPE_INTEGER, 124);
    sql:CallParam para2 = {sqlType:sql:TYPE_DATE, value:null, direction:sql:DIRECTION_INOUT};
    sql:CallParam para3 = {sqlType:sql:TYPE_TIME, value:null, direction:sql:DIRECTION_INOUT};
    sql:CallParam para4 = {sqlType:sql:TYPE_TIMESTAMP, value:null, direction:sql:DIRECTION_INOUT};
    sql:CallParam para5 = {sqlType:sql:TYPE_DATETIME, value:null, direction:sql:DIRECTION_INOUT};

    _ = testDB->call("{call TestDateINOUTParams(?,?,?,?,?)}", (), para1, para2, para3, para4, para5);
    _ = testDB->close();
    return (para2.value, para3.value, para4.value, para5.value);
}

function testDateTimeOutParams(int time, int date, int timestamp) returns (int) {
    endpoint sql:Client testDB {
        url:"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
    };

    sql:Parameter para1 = (sql:TYPE_INTEGER, 10);
    sql:Parameter para2 = (sql:TYPE_DATE, date);
    sql:Parameter para3 = (sql:TYPE_TIME, time);
    sql:Parameter para4 = (sql:TYPE_TIMESTAMP, timestamp);
    sql:Parameter para5 = (sql:TYPE_DATETIME, timestamp);

    sql:CallParam para6 = {sqlType:sql:TYPE_DATE, direction:sql:DIRECTION_OUT};
    sql:CallParam para7 = {sqlType:sql:TYPE_TIME, direction:sql:DIRECTION_OUT};
    sql:CallParam para8 = {sqlType:sql:TYPE_TIMESTAMP, direction:sql:DIRECTION_OUT};
    sql:CallParam para9 = {sqlType:sql:TYPE_DATETIME, direction:sql:DIRECTION_OUT};

    sql:Parameter[] parameters = [para1, para2, para3, para4, para5, para6, para7, para8, para9];

    _ = testDB->call("{call TestDateTimeOutParams(?,?,?,?,?,?,?,?,?)}", (),
        para1, para2, para3, para4, para5, para6, para7, para8, para9);

    table dt = check testDB->select("SELECT count(*) as countval from DateTimeTypes where row_id = 10", ResultCount);

    int count;
    while (dt.hasNext()) {
        ResultCount rs = check <ResultCount>dt.getNext();
        count = rs.COUNTVAL;
    }
    _ = testDB->close();
    return count;
}

function testStructOutParameters() returns (any) {
    endpoint sql:Client testDB {
        url:"jdbc:hsqldb:file:./target/tempdb/TEST_SQL_CONNECTOR",
        username:"SA",
        poolOptions:{maximumPoolSize:1}
    };

    sql:CallParam para1 = (sql:TYPE_STRUCT, sql:DIRECTION_OUT);
    _ = testDB->call("{call TestStructOut(?)}", (), para1);
    _ = testDB->close();
    return para1.value;
}

function testComplexTypeRetrieval() returns (string, string, string, string) {
    endpoint sql:Client testDB {
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
    s1 = io:sprintf("%l", [x1]);

    dt = check testDB->select("SELECT * from DateTimeTypes where row_id = 1", ());
    var x2 = check <xml>dt;
    s2 = io:sprintf("%l", [x2]);

    dt = check testDB->select("SELECT * from DataTypeTable where row_id = 1", ());
    var j = check <json>dt;
    s3 = io:sprintf("%j", [j]);

    dt = check testDB->select("SELECT * from DateTimeTypes where row_id = 1", ());
    j = check <json>dt;
    s4 = io:sprintf("%j", [j]);

    _ = testDB->close();
    return (s1, s2, s3, s4);
}

function testCloseConnectionPool() returns (int) {
    endpoint sql:Client testDB {
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
    _ = testDB->close();
    return count;
}
