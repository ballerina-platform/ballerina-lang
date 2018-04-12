import ballerina/sql;
import ballerina/io;
import ballerina/time;

type ResultPrimitive {
    int INT_TYPE;
    int LONG_TYPE;
    float FLOAT_TYPE;
    float DOUBLE_TYPE;
    boolean BOOLEAN_TYPE;
    string STRING_TYPE;
};

type ResultSetTestAlias {
    int INT_TYPE;
    int LONG_TYPE;
    float FLOAT_TYPE;
    float DOUBLE_TYPE;
    boolean BOOLEAN_TYPE;
    string STRING_TYPE;
    int DT2INT_TYPE;
};

type ResultObject {
    blob BLOB_TYPE;
    string CLOB_TYPE;
    blob BINARY_TYPE;
};

type ResultMap {
    int[] INT_ARRAY;
    int[] LONG_ARRAY;
    float[] FLOAT_ARRAY;
    boolean[] BOOLEAN_ARRAY;
    string[] STRING_ARRAY;
};

type ResultBlob {
    blob BLOB_TYPE;
};

type ResultDates {
    string DATE_TYPE;
    string TIME_TYPE;
    string TIMESTAMP_TYPE;
    string DATETIME_TYPE;
};

type ResultDatesStruct {
    time:Time DATE_TYPE;
    time:Time TIME_TYPE;
    time:Time TIMESTAMP_TYPE;
    time:Time DATETIME_TYPE;
};

type ResultDatesInt {
    int DATE_TYPE;
    int TIME_TYPE;
    int TIMESTAMP_TYPE;
    int DATETIME_TYPE;
};

type ResultSetFloat {
    float FLOAT_TYPE;
    float DOUBLE_TYPE;
    float NUMERIC_TYPE;
    float DECIMAL_TYPE;
};

type ResultPrimitiveInt {
    int INT_TYPE;
};

type ResultCount {
    int COUNTVAL;
};

type ResultTest {
    int t1Row;
    int t1Int;
    int t2Row;
    int t2Int;
};

type ResultSignedInt {
    int ID;
    int TINYINTDATA;
    int SMALLINTDATA;
    int INTDATA;
    int BIGINTDATA;
};

type ResultComplexTypes {
    int ROW_ID;
    blob BLOB_TYPE;
    string CLOB_TYPE;
    blob BINARY_TYPE;
};

type TestTypeData {
    int i;
    int[] iA;
    int l;
    int[] lA;
    float f;
    float[] fA;
    float d;
    boolean b;
    string s;
    float[] dA;
    boolean[] bA;
    string[] sA;
};

function testToJson () returns (json) {
    endpoint sql:Client testDB {
        url: "hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
        username: "SA",
        poolOptions: {maximumPoolSize:1}
    };

    sql:Parameter[] parameters = [];
    try {
        var dtRet = testDB -> select("SELECT int_type, long_type, float_type, double_type,
                  boolean_type, string_type from DataTable WHERE row_id = 1", ());
        table dt = check dtRet;
        return check <json>dt;
    } finally {
        _ = testDB -> close();
    }
    return null;
}

function testToXml () returns (xml) {
    endpoint sql:Client testDB {
        url: "hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
        username: "SA",
        poolOptions: {maximumPoolSize:1}
    };

    try {
        var dtRet = testDB -> select("SELECT int_type, long_type, float_type, double_type,
                   boolean_type, string_type from DataTable WHERE row_id = 1", ());
        table dt = check dtRet;

        return check <xml>dt;
    } finally {
        _ = testDB -> close();
    }
    return xml`<empty/>`;
}

function testToXmlMultipleConsume () returns (xml) {
    endpoint sql:Client testDB {
        url: "hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
        username: "SA",
        poolOptions: {maximumPoolSize:1}
    };

    try {
        var dtRet = testDB -> select("SELECT int_type, long_type, float_type, double_type,
        boolean_type, string_type from DataTable WHERE row_id = 1", ());
        table dt = check dtRet;

        xml result = check <xml>dt;
        io:println(result);
        return result;
    } finally {
        _ = testDB -> close();
    }
    return xml`<empty/>`;
}

function testToXmlWithAdd () returns (xml) {
    endpoint sql:Client testDB {
        url: "hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
        username: "SA",
        poolOptions: {maximumPoolSize:2}
    };

    try {
        var dt1Ret = testDB -> select("SELECT int_type from DataTable WHERE row_id = 1", ());
        table dt1 = check dt1Ret;
        xml result1 = check <xml>dt1;

        var dt2Ret = testDB -> select("SELECT int_type from DataTable WHERE row_id = 1", ());
        table dt2 = check dt2Ret;
        xml result2 = check <xml>dt2;

        xml result = result1 + result2;

        var dt3Ret = testDB -> select("SELECT int_type from DataTable WHERE row_id = 1", ());
        table dt3 = check dt3Ret;
        return result;
    } finally {
        _ = testDB -> close();
    }
    return xml`<empty/>`;
}

function testToJsonMultipleConsume () returns (json) {
    endpoint sql:Client testDB {
        url: "hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
        username: "SA",
        poolOptions: {maximumPoolSize:1}
    };

    try {
        var dtRet = testDB -> select("SELECT int_type, long_type, float_type, double_type,
        boolean_type, string_type from DataTable WHERE row_id = 1", ());
        table dt = check dtRet;

        json result = check <json>dt;
        io:println(result);
        return result;
    } finally {
        _ = testDB -> close();
    }
    return null;
}


function toXmlComplex () returns (xml) {
    endpoint sql:Client testDB {
        url: "hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
        username: "SA",
        poolOptions: {maximumPoolSize:1}
    };

    try {
        var dtRet = testDB -> select("SELECT int_type, int_array, long_type, long_array, float_type,
                    float_array, double_type, boolean_type, string_type, double_array, boolean_array, string_array
                    from MixTypes where row_id =1", ());
        table dt = check dtRet;

        return  check <xml>dt;
    } finally {
        _ = testDB -> close();
    }
    return xml`<empty/>`;
}

function testToXmlComplexWithStructDef () returns (xml) {
    endpoint sql:Client testDB {
        url: "hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
        username: "SA",
        poolOptions: {maximumPoolSize:1}
    };

    try {
        var dtRet = testDB -> select("SELECT int_type, int_array, long_type, long_array, float_type,
                    float_array, double_type, boolean_type, string_type, double_array, boolean_array, string_array
                    from MixTypes where row_id =1", TestTypeData);
        table dt = check dtRet;

        return check <xml>dt;
    } finally {
        _ = testDB -> close();
    }
    return xml`<empty/>`;
}


function testToJsonComplex () returns (json) {
    endpoint sql:Client testDB {
        url: "hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
        username: "SA",
        poolOptions: {maximumPoolSize:1}
    };

    try {
        var dtRet = testDB -> select("SELECT int_type, int_array, long_type, long_array, float_type,
                    float_array, double_type, boolean_type, string_type, double_array, boolean_array, string_array
                    from MixTypes where row_id =1", ());
        table dt = check dtRet;

        return check <json>dt;
    } finally {
        _ = testDB -> close();
    }
    return null;
}


function testToJsonComplexWithStructDef () returns (json) {
    endpoint sql:Client testDB {
        url: "hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
        username: "SA",
        poolOptions: {maximumPoolSize:1}
    };

    try {
        var dtRet = testDB -> select("SELECT int_type, int_array, long_type, long_array, float_type,
                    float_array, double_type, boolean_type, string_type, double_array, boolean_array, string_array
                    from MixTypes where row_id =1", TestTypeData);
        table dt = check dtRet;

        return check <json>dt;
    } finally {
        _ = testDB -> close();
    }
    return null;
}

function testJsonWithNull () returns (json) {
    endpoint sql:Client testDB {
        url: "hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
        username: "SA",
        poolOptions: {maximumPoolSize:1}
    };

    try {
        var dtRet =  testDB -> select("SELECT int_type, long_type, float_type, double_type,
                  boolean_type, string_type from DataTable WHERE row_id = 2", ());
        table dt = check dtRet;

        return check <json>dt;
    }  finally {
        _ = testDB -> close();
    }
    return null;
}

function testXmlWithNull () returns (xml) {
    endpoint sql:Client testDB {
        url: "hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
        username: "SA",
        poolOptions: {maximumPoolSize:1}
    };

    try {
        var dtRet = testDB -> select("SELECT int_type, long_type, float_type, double_type,
                   boolean_type, string_type from DataTable WHERE row_id = 2", ());
        table dt = check dtRet;

        return  check <xml>dt;
    } finally {
        _ = testDB -> close();
    }
    return xml`<empty/>`;
}

function testToXmlWithinTransaction () returns (string, int) {
    endpoint sql:Client testDB {
        url: "hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
        username: "SA",
        poolOptions: {maximumPoolSize:1}
    };

    int returnValue = 0;
    string resultXml;
    try {
        transaction {
            var dtRet = testDB -> select("SELECT int_type, long_type from DataTable WHERE row_id = 1", ());
            table dt = check dtRet;

            var result = check <xml>dt;
            resultXml = io:sprintf("%l", [result]);
        }
        return (resultXml, returnValue);
    } finally {
        _ = testDB -> close();
    }
    return ("<fail></fail>", -1);
}

function testToJsonWithinTransaction () returns (string, int) {
    endpoint sql:Client testDB {
        url: "hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
        username: "SA",
        poolOptions: {maximumPoolSize:1}
    };

    int returnValue = 0;
    string result;
    try {
        transaction {
            var dtRet = testDB -> select("SELECT int_type, long_type from DataTable WHERE row_id = 1", ());
            table dt = check dtRet;

            var j = check <json>dt;
            result = io:sprintf("%j", [j]);
        }
        return (result, returnValue);
    } finally {
        _ = testDB -> close();
    }
    return ("", -2);
}

function testGetPrimitiveTypes () returns (int, int, float, float , boolean, string) {
    endpoint sql:Client testDB {
        url: "hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
        username: "SA",
        poolOptions: {maximumPoolSize:1}
    };

    var dtRet = testDB -> select("SELECT int_type, long_type, float_type, double_type,
              boolean_type, string_type from DataTable WHERE row_id = 1", ResultPrimitive);
    table dt = check dtRet;

    int i;
    int l;
    float f;
    float d;
    boolean b;
    string s;
    while (dt.hasNext()) {
        ResultPrimitive rs = check <ResultPrimitive>dt.getNext();
        i = rs.INT_TYPE;
        l = rs.LONG_TYPE;
        f = rs.FLOAT_TYPE;
        d = rs.DOUBLE_TYPE;
        b = rs.BOOLEAN_TYPE;
        s = rs.STRING_TYPE;
    }
    _ = testDB -> close();
    return (i, l, f, d, b, s);
}

function testGetComplexTypes () returns (string, string, string) {
    endpoint sql:Client testDB {
        url: "hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
        username: "SA",
        poolOptions: {maximumPoolSize:1}
    };

    var dtRet = testDB -> select("SELECT blob_type,clob_type,binary_type from ComplexTypes where row_id = 1",ResultObject);
    table dt = check dtRet;

    string blobValue;
    string clob;
    string binary;
    while (dt.hasNext()) {
        ResultObject rs = check <ResultObject>dt.getNext();
        blob blobData = rs.BLOB_TYPE;
        blobValue = blobData.toString("UTF-8");
        clob = rs.CLOB_TYPE;
        blob binaryData = rs.BINARY_TYPE;
        binary = binaryData.toString("UTF-8");
    }
    _ = testDB -> close();
    return (blobValue, clob, binary);
}

function testArrayData () returns (int[], int[], float[], string[], boolean[]) {
    endpoint sql:Client testDB {
        url: "hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
        username: "SA",
        poolOptions: {maximumPoolSize:1}
    };

    var dtRet = testDB -> select("SELECT int_array, long_array, float_array, boolean_array,
              string_array from ArrayTypes where row_id = 1", ResultMap);
    table dt = check dtRet;

    int[] int_arr;
    int[] long_arr;
    float[] float_arr;
    string[] string_arr;
    boolean[] boolean_arr;

    while (dt.hasNext()) {
        ResultMap rs = check <ResultMap>dt.getNext();
        int_arr = rs.INT_ARRAY;
        long_arr = rs.LONG_ARRAY;
        float_arr = rs.FLOAT_ARRAY;
        boolean_arr = rs.BOOLEAN_ARRAY;
        string_arr = rs.STRING_ARRAY;
    }
    _ = testDB -> close();
    return (int_arr, long_arr, float_arr, string_arr, boolean_arr);
}

function testArrayDataInsertAndPrint () returns (int, int, int, int, int, int) {
    endpoint sql:Client testDB {
        url: "hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
        username: "SA",
        poolOptions: {maximumPoolSize:1}
    };

    int[] dataint = [1, 2, 3];
    float[] datafloat = [33.4, 55.4];
    string[] datastring = ["hello", "world"];
    boolean[] databoolean = [true, false, false, true, true];

    sql:Parameter paraID = (sql:TYPE_INTEGER, 4);
    sql:Parameter paraInt = (sql:TYPE_ARRAY, dataint);
    sql:Parameter paraLong = (sql:TYPE_ARRAY, dataint);
    sql:Parameter paraFloat = (sql:TYPE_ARRAY, datafloat);
    sql:Parameter paraString = (sql:TYPE_ARRAY, datastring);
    sql:Parameter paraBool = (sql:TYPE_ARRAY, databoolean);

   int intArrLen;
   int longArrLen;
   int floatArrLen;
   int boolArrLen;
   int strArrLen;

    var updateRetVal = testDB -> update("insert into ArrayTypes(row_id, int_array, long_array, float_array,
                                string_array, boolean_array) values (?,?,?,?,?,?)",
        paraID, paraInt, paraLong, paraFloat, paraString, paraBool);
    int updateRet = check updateRetVal;

    var dtRet = testDB -> select("SELECT int_array, long_array, float_array, boolean_array, string_array
                                 from ArrayTypes where row_id = 4", ResultMap);
    table dt = check dtRet;

    while (dt.hasNext()) {
        ResultMap rs = check <ResultMap>dt.getNext();
        io:println(rs.INT_ARRAY);
        intArrLen = lengthof rs.INT_ARRAY;
        io:println(rs.LONG_ARRAY);
        longArrLen = lengthof rs.LONG_ARRAY;
        io:println(rs.FLOAT_ARRAY);
        floatArrLen = lengthof rs.FLOAT_ARRAY;
        io:println(rs.BOOLEAN_ARRAY);
        boolArrLen = lengthof rs.BOOLEAN_ARRAY;
        io:println(rs.STRING_ARRAY);
        strArrLen = lengthof rs.STRING_ARRAY;
    }
    _ = testDB -> close();
    return (updateRet, intArrLen, longArrLen, floatArrLen, boolArrLen, strArrLen);
}

function testDateTime (int datein, int timein, int timestampin) returns (string, string, string, string) {
    endpoint sql:Client testDB {
        url: "hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
        username: "SA",
        poolOptions: {maximumPoolSize:1}
    };
    string date;
    string time;
    string timestamp;
    string datetime;

    sql:Parameter para0 = (sql:TYPE_INTEGER, 1);
    sql:Parameter para1 = (sql:TYPE_DATE, datein);
    sql:Parameter para2 = (sql:TYPE_TIME, timein);
    sql:Parameter para3 = (sql:TYPE_TIMESTAMP, timestampin);
    sql:Parameter para4 = (sql:TYPE_DATETIME, timestampin);

    var countRetRet = testDB -> update("Insert into DateTimeTypes
        (row_id, date_type, time_type, timestamp_type, datetime_type) values (?,?,?,?,?)",
        para0, para1, para2, para3, para4);
    int countRet = check countRetRet;

    var dtRet = testDB -> select("SELECT date_type, time_type, timestamp_type, datetime_type
                from DateTimeTypes where row_id = 1", ResultDates);
    table dt = check dtRet;

    while (dt.hasNext()) {
        ResultDates rs = check <ResultDates>dt.getNext();
        time = rs.TIME_TYPE;
        date = rs.DATE_TYPE;
        timestamp = rs.TIMESTAMP_TYPE;
        datetime = rs.DATETIME_TYPE;
    }
    _ = testDB -> close();
    return (date, time, timestamp, datetime);
}

function testDateTimeAsTimeStruct () returns (int, int, int, int, int, int, int, int ) {
    endpoint sql:Client testDB {
        url: "hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
        username: "SA",
        poolOptions: {maximumPoolSize:1}
    };

    int dateInserted;
    int dateRetrieved;
    int timeInserted;
    int timeRetrieved;
    int timestampInserted;
    int timestampRetrieved;
    int datetimeInserted;
    int datetimeRetrieved;

    time:Time dateStruct = time:createTime(2017, 5, 23, 0, 0, 0, 0, "");

    time:Timezone zoneValue = {zoneId:"UTC"};
    time:Time timeStruct =new (51323000, zoneValue);

    time:Time timestampStruct = time:createTime(2017, 1, 25, 16, 12, 23, 0, "UTC");
    time:Time datetimeStruct = time:createTime(2017, 1, 31, 16, 12, 23, 332, "UTC");
    dateInserted = dateStruct.time;
    timeInserted = timeStruct.time;
    timestampInserted = timestampStruct.time;
    datetimeInserted = datetimeStruct.time;

    sql:Parameter para0 = (sql:TYPE_INTEGER, 31);
    sql:Parameter para1 = (sql:TYPE_DATE, dateStruct);
    sql:Parameter para2 = (sql:TYPE_TIME, timeStruct);
    sql:Parameter para3 = (sql:TYPE_TIMESTAMP, timestampStruct);
    sql:Parameter para4 = (sql:TYPE_DATETIME, datetimeStruct);

    var countRet = testDB -> update("Insert into DateTimeTypes
        (row_id, date_type, time_type, timestamp_type, datetime_type) values (?,?,?,?,?)",
        para0, para1, para2, para3, para4);
    int count = check countRet;

    var dtRet = testDB -> select("SELECT date_type, time_type, timestamp_type, datetime_type
                from DateTimeTypes where row_id = 31", ResultDatesStruct);
    table dt = check dtRet;

    while (dt.hasNext()) {
        ResultDatesStruct rs = check <ResultDatesStruct>dt.getNext();
        dateRetrieved = rs.DATE_TYPE.time;
        timeRetrieved = rs.TIME_TYPE.time;
        timestampRetrieved = rs.TIMESTAMP_TYPE.time;
        datetimeRetrieved = rs.DATETIME_TYPE.time;
    }
     _ = testDB -> close();
    return (dateInserted, dateRetrieved, timeInserted, timeRetrieved, timestampInserted, timestampRetrieved,
        datetimeInserted, datetimeRetrieved);
}

function testDateTimeInt (int datein, int timein, int timestampin) returns (int, int, int, int) {
    endpoint sql:Client testDB {
        url: "hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
        username: "SA",
        poolOptions: {maximumPoolSize:1}
    };

    sql:Parameter para0 = (sql:TYPE_INTEGER, 32);
    sql:Parameter para1 = (sql:TYPE_DATE, datein);
    sql:Parameter para2 = (sql:TYPE_TIME, timein);
    sql:Parameter para3 = (sql:TYPE_TIMESTAMP, timestampin);
    sql:Parameter para4 = (sql:TYPE_DATETIME, timestampin);

    int  date;
    int time;
    int timestamp;
    int datetime;

    var countRet = testDB -> update("Insert into DateTimeTypes
        (row_id, date_type, time_type, timestamp_type, datetime_type) values (?,?,?,?,?)",
        para0, para1, para2, para3, para4);
    int countt = check countRet;

    var dtRet = testDB -> select("SELECT date_type, time_type, timestamp_type, datetime_type
                from DateTimeTypes where row_id = 32", ResultDatesInt);
    table<ResultDatesInt> dt = check dtRet;

    while (dt.hasNext()) {
        ResultDatesInt rs = check <ResultDatesInt>dt.getNext();
        time = rs.TIME_TYPE;
        date = rs.DATE_TYPE;
        timestamp = rs.TIMESTAMP_TYPE;
        datetime = rs.DATETIME_TYPE;
    }
    _ = testDB -> close();
    return (date, time, timestamp, datetime);
}

function testBlobData () returns (string) {
    endpoint sql:Client testDB {
        url: "hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
        username: "SA",
        poolOptions: {maximumPoolSize:1}
    };

    string blobStringData;
    var dtRet = testDB -> select("SELECT blob_type from ComplexTypes where row_id = 1", ResultBlob);
    table dt = check dtRet;

    blob blobData;
    while (dt.hasNext()) {
        ResultBlob rs = check <ResultBlob>dt.getNext();
        blobData = rs.BLOB_TYPE;
    }
    blobStringData = blobData.toString("UTF-8");

    _ = testDB -> close();
    return blobStringData;
}

function testColumnAlias () returns (int, int, float, float, boolean, string, int) {
    endpoint sql:Client testDB {
        url: "hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
        username: "SA",
        poolOptions: {maximumPoolSize:1}
    };

    var dtRet = testDB -> select("SELECT dt1.int_type, dt1.long_type, dt1.float_type,
           dt1.double_type,dt1.boolean_type, dt1.string_type,dt2.int_type as dt2int_type from DataTable dt1
           left join DataTableRep dt2 on dt1.row_id = dt2.row_id WHERE dt1.row_id = 1;", ResultSetTestAlias);
    table dt = check dtRet;

    int i;
    int l;
    float f;
    float d;
    boolean b;
    string s;
    int i2;

    while (dt.hasNext()) {
        ResultSetTestAlias rs = check <ResultSetTestAlias>dt.getNext();
        i = rs.INT_TYPE;
        l = rs.LONG_TYPE;
        f = rs.FLOAT_TYPE;
        d = rs.DOUBLE_TYPE;
        b = rs.BOOLEAN_TYPE;
        s = rs.STRING_TYPE;
        i2 = rs.DT2INT_TYPE;
    }
    _ = testDB -> close();
    return (i, l, f, d, b, s, i2);
}

function testBlobInsert () returns (int) {
    endpoint sql:Client testDB {
        url: "hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
        username: "SA",
        poolOptions: {maximumPoolSize:1}
    };

    var dtRet = testDB -> select("SELECT blob_type from ComplexTypes where row_id = 1", ResultBlob);
    table dt = check dtRet;

    blob blobData;
    while (dt.hasNext()) {
        ResultBlob rs = check <ResultBlob>dt.getNext();
        blobData = rs.BLOB_TYPE;
    }
    sql:Parameter para0 = (sql:TYPE_INTEGER, 10);
    sql:Parameter para1 = (sql:TYPE_BLOB, blobData);
    var insertCountRet = testDB -> update("Insert into ComplexTypes (row_id, blob_type) values (?,?)", para0, para1);
    int insertCount = check insertCountRet;

    _ = testDB -> close();
    return insertCount;
}


function testTableAutoClose () returns (int, json) {
    endpoint sql:Client testDB {
        url: "hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
        username: "SA",
        poolOptions: {maximumPoolSize:1}
    };

    var dtRet = testDB -> select("SELECT int_type from DataTable WHERE row_id = 1", ResultPrimitiveInt);
    table dt = check dtRet;

    int i;
    string test;
    while (dt.hasNext()) {
        var rs = check <ResultPrimitiveInt>dt.getNext();
        i = rs.INT_TYPE;
    }

    var dt2Ret = testDB -> select("SELECT int_type, long_type, float_type, double_type,
              boolean_type, string_type from DataTable WHERE row_id = 1", ());
    table dt2 = check dt2Ret;

    json jsonData = check <json> dt2;
    _ = jsonData.remove("int_type");

    _ = testDB -> select("SELECT int_type, long_type, float_type, double_type,
              boolean_type, string_type from DataTable WHERE row_id = 1", ());

    _ = testDB -> close();
    return (i, jsonData);
}

function testTableManualClose () returns (int) {
    endpoint sql:Client testDB {
        url: "hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
        username: "SA",
        poolOptions: {maximumPoolSize:1}
    };

    var dtRet = testDB -> select("SELECT int_type from DataTable", ResultPrimitiveInt);
    table dt = check dtRet;

    int i = 0;
    while (dt.hasNext()) {
        ResultPrimitiveInt rs = check <ResultPrimitiveInt>dt.getNext();
        int ret = rs.INT_TYPE;
        i = i + 1;
        if (i == 1) {
            break;
        }
    }
    dt.close();

    int data;
    var dt2Ret = testDB -> select("SELECT int_type from DataTable WHERE row_id = 1", ResultPrimitiveInt);
    table dt2 = check dt2Ret;

    while (dt2.hasNext()) {
        ResultPrimitiveInt rs2 = check <ResultPrimitiveInt>dt2.getNext();
        data = rs2.INT_TYPE;
    }
    dt2.close();
    _ = testDB -> close();
    return data;
}

function testCloseConnectionPool () returns (int) {
    endpoint sql:Client testDB {
        url: "hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
        username: "SA",
        poolOptions: {maximumPoolSize:1}
    };

    var dtRet = testDB -> select ("SELECT COUNT(*) as countVal FROM INFORMATION_SCHEMA.SYSTEM_SESSIONS", ResultCount);
    table dt = check dtRet;

    int count;
    while (dt.hasNext()) {
        ResultCount rs = check <ResultCount> dt.getNext();
        count = rs.COUNTVAL;
    }
    _ = testDB -> close();
    return count;
}

function testTablePrintAndPrintln() {
    endpoint sql:Client testDB {
        url: "hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
        username: "SA",
        poolOptions: {maximumPoolSize:1}
    };

    var dtRet = testDB -> select("SELECT int_type, long_type, float_type, double_type,
        boolean_type, string_type from DataTable WHERE row_id = 1", ());
    table dt = check dtRet;

    io:println(dt);
    io:print(dt);
    _ = testDB -> close();
}

function testMutltipleRows () returns (int, int) {
    endpoint sql:Client testDB {
        url: "hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
        username: "SA",
        poolOptions: {maximumPoolSize:1}
    };

    var dtRet = testDB -> select("SELECT int_type from DataTableRep", ResultPrimitiveInt);
    table dt = check dtRet;

    ResultPrimitiveInt rs1 = {INT_TYPE:-1};
    ResultPrimitiveInt rs2 = {INT_TYPE:-1};
    int i = 0;
    while (dt.hasNext()) {
        if (i == 0) {
            rs1 = check <ResultPrimitiveInt>dt.getNext();
        } else {
            rs2 = check <ResultPrimitiveInt>dt.getNext();
        }
        i = i + 1;
    }
    _ = testDB -> close();
    return (rs1.INT_TYPE, rs2.INT_TYPE);
}

function testMutltipleRowsWithoutLoop () returns (int, int, int, int, string, string) {
    endpoint sql:Client testDB {
        url: "hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
        username: "SA",
        poolOptions: {maximumPoolSize:1}
    };

    //Iterate the whole result
    var dtRet = testDB -> select("SELECT int_type from DataTableRep order by int_type desc", ResultPrimitiveInt);
    table dt = check dtRet;

    int i1;
    int i2;
    int i3;
    int i4;
    string st1;
    string st2;
    while (dt.hasNext()) {
        ResultPrimitiveInt rs = check <ResultPrimitiveInt>dt.getNext();
        i1 = rs.INT_TYPE;
    }

    //Pick the first row only
    dtRet = testDB -> select("SELECT int_type from DataTableRep order by int_type desc", ResultPrimitiveInt);
    dt = check dtRet;

    if (dt.hasNext()) {
        ResultPrimitiveInt rs = check <ResultPrimitiveInt>dt.getNext();
        i2 = rs.INT_TYPE;
    }
    dt.close();

    //Pick all the rows without checking
    dtRet = testDB -> select("SELECT int_type from DataTableRep order by int_type desc", ResultPrimitiveInt);
    dt = check dtRet;

    ResultPrimitiveInt rs1 = check <ResultPrimitiveInt>dt.getNext();
    i3 = rs1.INT_TYPE;

    ResultPrimitiveInt rs2 = check <ResultPrimitiveInt>dt.getNext();
    i4 = rs2.INT_TYPE;
    dt.close();

    //Pick the first row by checking and next row without checking
    string s1 = "";
    dtRet = testDB -> select("SELECT int_type from DataTableRep order by int_type desc", ResultPrimitiveInt);
    dt = check dtRet;

    if (dt.hasNext()) {
        ResultPrimitiveInt rs = check <ResultPrimitiveInt>dt.getNext();
        int i = rs.INT_TYPE;
        s1 = s1 + i;
    }

    ResultPrimitiveInt rs = check <ResultPrimitiveInt>dt.getNext();
    int i = rs.INT_TYPE;
    s1 = s1 + "_" + i;

    if (dt.hasNext()) {
        s1 = s1 + "_" + "HAS";
    } else {
        s1 = s1 + "_" + "NOT";
    }

    //Pick the first row without checking, then check and no fetch, and finally fetch row by checking
    string s2 = "";
    dtRet = testDB -> select("SELECT int_type from DataTableRep order by int_type desc", ResultPrimitiveInt);
    dt = check dtRet;

    rs = check <ResultPrimitiveInt>dt.getNext();
    i = rs.INT_TYPE;
    s2 = s2 + i;
    if (dt.hasNext()) {
        s2 = s2 + "_" + "HAS";
    } else {
        s2 = s2 + "_" + "NO";
    }
    if (dt.hasNext()) {
        s2 = s2 + "_" + "HAS";
    } else {
        s2 = s2 + "_" + "NO";
    }
    if (dt.hasNext()) {
        rs = check <ResultPrimitiveInt>dt.getNext();
        i = rs.INT_TYPE;
        s2 = s2 + "_" + i;
    }
    if (dt.hasNext()) {
        s2 = s2 + "_" + "HAS";
    } else {
        s2 = s2 + "_" + "NO";
    }
    if (dt.hasNext()) {
        s2 = s2 + "_" + "HAS";
    } else {
        s2 = s2 + "_" + "NO";
    }
    _ = testDB -> close();
    return (i1, i2, i3, i4, s1, s2);
}

function testHasNextWithoutConsume () returns (boolean, boolean, boolean) {
    endpoint sql:Client testDB {
        url: "hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
        username: "SA",
        poolOptions: {maximumPoolSize:1}
    };

    var dtRet = testDB -> select("SELECT int_type from DataTableRep order by int_type desc", ResultPrimitiveInt);
    table dt = check dtRet;

    boolean b1 = false;
    boolean b2 = false;
    boolean b3 = false;

    if (dt.hasNext()) {
        b1 = true;
    }
    if (dt.hasNext()) {
        b2 = true;
    }
    if (dt.hasNext()) {
        b3 = true;
    }
    _ = testDB -> close();
    return (b1, b2, b3);
}

function testGetFloatTypes () returns (float, float, float, float) {
    endpoint sql:Client testDB {
        url: "hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
        username: "SA",
        poolOptions: {maximumPoolSize:1}
    };

    var dtRet = testDB -> select("SELECT float_type, double_type,
                  numeric_type, decimal_type from FloatTable WHERE row_id = 1", ResultSetFloat);
    table dt = check dtRet;

    float f;
    float d;
    float num;
    float dec;

    while (dt.hasNext()) {
        var rs = check <ResultSetFloat>dt.getNext();
        f = rs.FLOAT_TYPE;
        d = rs.DOUBLE_TYPE;
        num = rs.NUMERIC_TYPE;
        dec = rs.DECIMAL_TYPE;
    }
    _ = testDB -> close();
    return (f, d, num, dec);
}

function testSignedIntMaxMinValues () returns (int, int, int, string, string, string) {
    endpoint sql:Client testDB {
        url: "hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
        username: "SA",
        poolOptions: {maximumPoolSize:1}
    };

    string insertSQL = "INSERT INTO IntegerTypes(id,tinyIntData, smallIntData, intData, bigIntData) VALUES (?,?, ?,?,?)";
    string selectSQL = "SELECT id,tinyIntData,smallIntData,intData,bigIntData FROM IntegerTypes";

    int maxInsert;
    int minInsert;
    int nullInsert;
    string jsonStr;
    string xmlStr;
    string str;

    //Insert signed max
    sql:Parameter para1 = (sql:TYPE_INTEGER, 1);
    sql:Parameter para2 = (sql:TYPE_TINYINT, 127);
    sql:Parameter para3 = (sql:TYPE_SMALLINT, 32767);
    sql:Parameter para4 = (sql:TYPE_INTEGER, 2147483647);
    sql:Parameter para5 = (sql:TYPE_BIGINT, 9223372036854775807);
    var maxInsertRet = testDB -> update(insertSQL, para1, para2, para3, para4, para5);
    maxInsert = check maxInsertRet;

    //Insert signed min
    para1 = (sql:TYPE_INTEGER, 2);
    para2 = (sql:TYPE_TINYINT, -128);
    para3 = (sql:TYPE_SMALLINT, -32768);
    para4 = (sql:TYPE_INTEGER, -2147483648);
    para5 = (sql:TYPE_BIGINT, -9223372036854775808);
    var minInsertRet =  testDB -> update(insertSQL, para1, para2, para3, para4, para5);
    minInsert = check minInsertRet;

    //Insert null
    para1 = (sql:TYPE_INTEGER, 3);
    para2 = (sql:TYPE_TINYINT, null);
    para3 = (sql:TYPE_SMALLINT, null);
    para4 = (sql:TYPE_INTEGER, null);
    para5 = (sql:TYPE_BIGINT, null);
    var nullInsertRet = testDB -> update(insertSQL, para1, para2, para3, para4, para5);
    nullInsert = check nullInsertRet;

    var dtRet = testDB -> select(selectSQL, ());
    table dt = check dtRet;

    var j = check <json>dt;
    jsonStr = io:sprintf("%j", [j]);

    dtRet = testDB -> select(selectSQL, ());
    dt = check dtRet;

    var x = check <xml>dt;
    xmlStr = io:sprintf("%l", [x]);

    dtRet = testDB -> select(selectSQL, ResultSignedInt);
    dt = check dtRet;

    str = "";
    while (dt.hasNext()) {
        var result = check <ResultSignedInt>dt.getNext();
        str = str + result.ID + "|" + result.TINYINTDATA + "|" + result.SMALLINTDATA + "|" + result.INTDATA + "|" +
              result.BIGINTDATA + "#";
    }
    _ = testDB -> close();
    return (maxInsert, minInsert, nullInsert, jsonStr, xmlStr, str);
}

function testComplexTypeInsertAndRetrieval () returns (int, int, string, string, string) {
    endpoint sql:Client testDB {
        url: "hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
        username: "SA",
        poolOptions: {maximumPoolSize:1}
    };

    string insertSQL = "INSERT INTO ComplexTypes(row_id, blob_type, clob_type, binary_type) VALUES (?,?,?,?)";
    string selectSQL = "SELECT row_id, blob_type, clob_type, binary_type FROM ComplexTypes where row_id = 100 or row_id = 200";
    string text = "Sample Text";
    blob content = text.toBlob("UTF-8");

    int retDataInsert;
    int retNullInsert;
    string jsonStr;
    string xmlStr;
    string str;

    //Insert data
    sql:Parameter para1 = (sql:TYPE_INTEGER, 100);
    sql:Parameter para2 = (sql:TYPE_BLOB, content);
    sql:Parameter para3 = (sql:TYPE_CLOB, text);
    sql:Parameter para4 = (sql:TYPE_BINARY, content);
    var retDataInsertRet = testDB -> update(insertSQL, para1, para2, para3, para4);
    retDataInsert = check retDataInsertRet;

    //Insert null values
    para1 = (sql:TYPE_INTEGER, 200);
    para2 = (sql:TYPE_BLOB, null);
    para3 = (sql:TYPE_CLOB, null);
    para4 = (sql:TYPE_BINARY, null);
    var retNullInsertRet = testDB -> update(insertSQL, para1, para2, para3, para4);
    retNullInsert = check retNullInsertRet;

    var dtRet = testDB -> select(selectSQL, ());
    table dt = check dtRet;

    var j = check <json>dt;
    jsonStr = io:sprintf("%j", [j]);

    dtRet = testDB -> select(selectSQL, ());
    dt = check dtRet;

    var x = check <xml>dt;
    xmlStr = io:sprintf("%l", [x]);

    dtRet = testDB -> select(selectSQL, ResultComplexTypes);
    dt = check dtRet;

    str = "";
    while (dt.hasNext()) {
        var result = check <ResultComplexTypes>dt.getNext();
        str = str + result.ROW_ID + "|" + result.BLOB_TYPE.toString("UTF-8") + "|" + result.CLOB_TYPE + "|";
    }
    _ = testDB -> close();
    return (retDataInsert, retNullInsert, jsonStr, xmlStr, str);
}

function testJsonXMLConversionwithDuplicateColumnNames () returns (json, xml) {
    endpoint sql:Client testDB {
        url: "hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
        username: "SA",
        poolOptions: {maximumPoolSize:2}
    };

    var dtRet = testDB -> select("SELECT dt1.row_id, dt1.int_type, dt2.row_id, dt2.int_type from DataTable dt1 left
            join DataTableRep dt2 on dt1.row_id = dt2.row_id WHERE dt1.row_id = 1", ());
    table dt = check dtRet;

    json j = check <json> dt;

    var dt2Ret = testDB -> select("SELECT dt1.row_id, dt1.int_type, dt2.row_id, dt2.int_type from DataTable dt1 left
            join DataTableRep dt2 on dt1.row_id = dt2.row_id WHERE dt1.row_id = 1", ());
    table dt2 = check dt2Ret;

    xml x = check <xml> dt2;

    _ = testDB -> close();
    return (j, x);
}

function testStructFieldNotMatchingColumnName () returns (int, int, int, int, int) {
    endpoint sql:Client testDB {
        url: "hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
        username: "SA",
        poolOptions: {maximumPoolSize:1}
    };

    var dtRet = testDB -> select("SELECT count(*) from DataTable WHERE row_id = 1", ResultCount);
    table dt = check dtRet;

    int countAll;
    int i1;
    int i2;
    int i3;
    int i4;
    while (dt.hasNext()) {
        ResultCount rs = check <ResultCount>dt.getNext();
        countAll = rs.COUNTVAL;
    }

    var dt2Ret = testDB -> select("SELECT dt1.row_id, dt1.int_type, dt2.row_id, dt2.int_type from DataTable dt1 left
            join DataTableRep dt2 on dt1.row_id = dt2.row_id WHERE dt1.row_id = 1", ResultTest);
    table dt2 = check dt2Ret;

    while (dt2.hasNext()) {
        ResultTest rs = check <ResultTest>dt2.getNext();
        i1 = rs.t1Row;
        i2 = rs.t1Int;
        i3 = rs.t2Row;
        i4 = rs.t2Int;
    }
    _ = testDB -> close();
    return (countAll, i1, i2, i3, i4);
}

function testGetPrimitiveTypesWithForEach () returns (int, int, float, float, boolean, string) {
    endpoint sql:Client testDB {
        url: "hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
        username: "SA",
        poolOptions: {maximumPoolSize:1}
    };

    var dt1Ret = testDB -> select("SELECT int_type, long_type, float_type, double_type,
              boolean_type, string_type from DataTable WHERE row_id = 1", ResultPrimitive);
    table<ResultPrimitive> dt = check dt1Ret;

    int i;
    int l;
    float f;
    float d;
    boolean b;
    string s;
    foreach x in dt {
        i = x.INT_TYPE;
        l = x.LONG_TYPE;
        f = x.FLOAT_TYPE;
        d = x.DOUBLE_TYPE;
        b = x.BOOLEAN_TYPE;
        s = x.STRING_TYPE;
    }
    _ = testDB -> close();
    return (i, l, f, d, b, s);
}

function testMutltipleRowsWithForEach () returns (int, int) {
    endpoint sql:Client testDB {
        url: "hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
        username: "SA",
        poolOptions: {maximumPoolSize:1}
    };

    var dt1Ret = testDB -> select("SELECT int_type from DataTableRep", ResultPrimitiveInt);
    table<ResultPrimitiveInt> dt = check dt1Ret;

    ResultPrimitiveInt rs1 = {INT_TYPE: -1};
    ResultPrimitiveInt rs2 = {INT_TYPE: -1};
    int i = 0;
    foreach x in dt {
        if (i == 0) {
            rs1 = x;
        } else {
            rs2 = x;
        }
        i = i + 1;
    }
    _ = testDB -> close();
    return (rs1.INT_TYPE, rs2.INT_TYPE);
}

function testTableAddInvalid () {
    endpoint sql:Client testDB {
        url: "hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
        username: "SA",
        poolOptions: {maximumPoolSize:1}
    };

    var dtRet = testDB -> select("SELECT int_type from DataTableRep", ResultPrimitiveInt);
    table dt = check dtRet;

    try {
        ResultPrimitiveInt row = {INT_TYPE:443};
        _ = dt.add(row);
    } finally {
        _ = testDB -> close();
    }
}

function testTableRemoveInvalid () {
    endpoint sql:Client testDB {
        url: "hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
        username: "SA",
        poolOptions: {maximumPoolSize:1}
    };

    var dtRet = testDB -> select("SELECT int_type from DataTableRep", ResultPrimitiveInt);
    table dt = check dtRet;

    try {
        ResultPrimitiveInt row = {INT_TYPE:443};
        _ = dt.remove(isDelete);
    } finally {
       _ = testDB -> close();
    }
}

function isDelete (ResultPrimitiveInt p) returns (boolean) {
    return p.INT_TYPE < 2000;
}
