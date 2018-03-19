import ballerina.data.sql;
import ballerina.io;
import ballerina.time;

struct ResultPrimitive {
    int INT_TYPE;
    int LONG_TYPE;
    float FLOAT_TYPE;
    float DOUBLE_TYPE;
    boolean BOOLEAN_TYPE;
    string STRING_TYPE;
}

struct ResultSetTestAlias {
    int INT_TYPE;
    int LONG_TYPE;
    float FLOAT_TYPE;
    float DOUBLE_TYPE;
    boolean BOOLEAN_TYPE;
    string STRING_TYPE;
    int DT2INT_TYPE;
}

struct ResultObject {
    blob BLOB_TYPE;
    string CLOB_TYPE;
    blob BINARY_TYPE;
}

struct ResultMap {
    int[] INT_ARRAY;
    int[] LONG_ARRAY;
    float[] FLOAT_ARRAY;
    boolean[] BOOLEAN_ARRAY;
    string[] STRING_ARRAY;
}

struct ResultBlob {
    blob BLOB_TYPE;
}

struct ResultDates {
    string DATE_TYPE;
    string TIME_TYPE;
    string TIMESTAMP_TYPE;
    string DATETIME_TYPE;
}

struct ResultDatesStruct {
    time:Time DATE_TYPE;
    time:Time TIME_TYPE;
    time:Time TIMESTAMP_TYPE;
    time:Time DATETIME_TYPE;
}

struct ResultDatesInt {
    int DATE_TYPE;
    int TIME_TYPE;
    int TIMESTAMP_TYPE;
    int DATETIME_TYPE;
}

struct ResultSetFloat {
    float FLOAT_TYPE;
    float DOUBLE_TYPE;
    float NUMERIC_TYPE;
    float DECIMAL_TYPE;
}

struct ResultPrimitiveInt {
    int INT_TYPE;
}

struct ResultCount {
    int COUNTVAL;
}

struct ResultTest {
    int t1Row;
    int t1Int;
    int t2Row;
    int t2Int;
}

struct ResultSignedInt {
    int ID;
    int TINYINTDATA;
    int SMALLINTDATA;
    int INTDATA;
    int BIGINTDATA;
}

struct ResultComplexTypes {
    int ROW_ID;
    blob BLOB_TYPE;
    string CLOB_TYPE;
    blob BINARY_TYPE;
}

struct TestTypeData {
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
}

function testToJson () (json) {
    endpoint sql:Client testDB {
        database: sql:DB.HSQLDB_FILE,
        host: "./target/tempdb/",
        port: 0,
        name: "TEST_DATA_TABLE_DB",
        username: "SA",
        password: "",
        options: {maximumPoolSize:1}
    };

    sql:Parameter[] parameters = [];
    try {
        table dt = testDB -> select("SELECT int_type, long_type, float_type, double_type,
                  boolean_type, string_type from DataTable WHERE row_id = 1", parameters, null);
        json result;
        result, _ = <json>dt;
        return result;
    } finally {
        testDB -> close();
    }
    return null;
}

function testToXml () (xml) {
    endpoint sql:Client testDB {
        database: sql:DB.HSQLDB_FILE,
        host: "./target/tempdb/",
        port: 0,
        name: "TEST_DATA_TABLE_DB",
        username: "SA",
        password: "",
        options: {maximumPoolSize:1}
    };

    try {
        table dt = testDB -> select("SELECT int_type, long_type, float_type, double_type,
                   boolean_type, string_type from DataTable WHERE row_id = 1", null, null);
        xml result;
        result, _ = <xml>dt;
        return result;
    } finally {
        testDB -> close();
    }
    return null;
}

function testToXmlMultipleConsume () (xml) {
    endpoint sql:Client testDB {
        database: sql:DB.HSQLDB_FILE,
        host: "./target/tempdb/",
        port: 0,
        name: "TEST_DATA_TABLE_DB",
        username: "SA",
        password: "",
        options: {maximumPoolSize:1}
    };

    try {
        table dt = testDB -> select("SELECT int_type, long_type, float_type, double_type,
        boolean_type, string_type from DataTable WHERE row_id = 1", null, null);
        xml result;
        result, _ = <xml>dt;
        io:println(result);
        return result;
    } finally {
        testDB -> close();
    }
    return null;
}

function testToXmlWithAdd () (xml) {
    endpoint sql:Client testDB {
        database: sql:DB.HSQLDB_FILE,
        host: "./target/tempdb/",
        port: 0,
        name: "TEST_DATA_TABLE_DB",
        username: "SA",
        password: "",
        options: {maximumPoolSize:2}
    };

    try {
        table dt1 = testDB -> select("SELECT int_type from DataTable WHERE row_id = 1", null, null);
        var result1, _ = <xml>dt1;

        table dt2 = testDB -> select("SELECT int_type from DataTable WHERE row_id = 1", null, null);
        var result2, _ = <xml>dt2;

        xml result = result1 + result2;

        table dt3 = testDB -> select("SELECT int_type from DataTable WHERE row_id = 1", null, null);
        return result;
    } finally {
        testDB -> close();
    }
    return null;
}

function testToJsonMultipleConsume () (json) {
    endpoint sql:Client testDB {
        database: sql:DB.HSQLDB_FILE,
        host: "./target/tempdb/",
        port: 0,
        name: "TEST_DATA_TABLE_DB",
        username: "SA",
        password: "",
        options: {maximumPoolSize:1}
    };

    try {
        table dt = testDB -> select("SELECT int_type, long_type, float_type, double_type,
        boolean_type, string_type from DataTable WHERE row_id = 1", null, null);
        json result;
        result, _ = <json>dt;
        io:println(result);
        return result;
    } finally {
        testDB -> close();
    }
    return null;
}


function toXmlComplex () (xml) {
    endpoint sql:Client testDB {
        database: sql:DB.HSQLDB_FILE,
        host: "./target/tempdb/",
        port: 0,
        name: "TEST_DATA_TABLE_DB",
        username: "SA",
        password: "",
        options: {maximumPoolSize:1}
    };

    try {
        table dt = testDB -> select("SELECT int_type, int_array, long_type, long_array, float_type,
                    float_array, double_type, boolean_type, string_type, double_array, boolean_array, string_array
                    from MixTypes where row_id =1", null, null);
        xml result;
        result, _ = <xml>dt;
        return result;
    } finally {
        testDB -> close();
    }
    return null;
}

function testToXmlComplexWithStructDef () (xml) {
    endpoint sql:Client testDB {
        database: sql:DB.HSQLDB_FILE,
        host: "./target/tempdb/",
        port: 0,
        name: "TEST_DATA_TABLE_DB",
        username: "SA",
        password: "",
        options: {maximumPoolSize:1}
    };

    try {
        table dt = testDB -> select("SELECT int_type, int_array, long_type, long_array, float_type,
                    float_array, double_type, boolean_type, string_type, double_array, boolean_array, string_array
                    from MixTypes where row_id =1", null, typeof TestTypeData);
        xml result;
        result, _ = <xml>dt;
        return result;
    } finally {
        testDB -> close();
    }
    return null;
}


function testToJsonComplex () (json) {
    endpoint sql:Client testDB {
        database: sql:DB.HSQLDB_FILE,
        host: "./target/tempdb/",
        port: 0,
        name: "TEST_DATA_TABLE_DB",
        username: "SA",
        password: "",
        options: {maximumPoolSize:1}
    };

    try {
        table dt = testDB -> select("SELECT int_type, int_array, long_type, long_array, float_type,
                    float_array, double_type, boolean_type, string_type, double_array, boolean_array, string_array
                    from MixTypes where row_id =1", null, null);
        json result;
        result, _ = <json>dt;
        return result;
    } finally {
        testDB -> close();
    }
    return null;
}


function testToJsonComplexWithStructDef () (json) {
    endpoint sql:Client testDB {
        database: sql:DB.HSQLDB_FILE,
        host: "./target/tempdb/",
        port: 0,
        name: "TEST_DATA_TABLE_DB",
        username: "SA",
        password: "",
        options: {maximumPoolSize:1}
    };

    try {
        table dt = testDB -> select("SELECT int_type, int_array, long_type, long_array, float_type,
                    float_array, double_type, boolean_type, string_type, double_array, boolean_array, string_array
                    from MixTypes where row_id =1", null, typeof TestTypeData);
        json result;
        result, _ = <json>dt;
        return result;
    } finally {
        testDB -> close();
    }
    return null;
}

function testJsonWithNull () (json) {
    endpoint sql:Client testDB {
        database: sql:DB.HSQLDB_FILE,
        host: "./target/tempdb/",
        port: 0,
        name: "TEST_DATA_TABLE_DB",
        username: "SA",
        password: "",
        options: {maximumPoolSize:1}
    };

    try {
        table dt = testDB -> select("SELECT int_type, long_type, float_type, double_type,
                  boolean_type, string_type from DataTable WHERE row_id = 2", null, null);
        json result;
        result, _ = <json>dt;
        return result;
    }  finally {
        testDB -> close();
    }
    return null;
}

function testXmlWithNull () (xml) {
    endpoint sql:Client testDB {
        database: sql:DB.HSQLDB_FILE,
        host: "./target/tempdb/",
        port: 0,
        name: "TEST_DATA_TABLE_DB",
        username: "SA",
        password: "",
        options: {maximumPoolSize:1}
    };

    try {
        table dt = testDB -> select("SELECT int_type, long_type, float_type, double_type,
                   boolean_type, string_type from DataTable WHERE row_id = 2", null, null);
        xml result;
        result, _ = <xml>dt;
        return result;
    } finally {
        testDB -> close();
    }
    return null;
}

//function testToXmlWithinTransaction () (string, int) {
//    endpoint sql:Client testDB {
//        database: sql:DB.HSQLDB_FILE,
//        host: "./target/tempdb/",
//        port: 0,
//        name: "TEST_DATA_TABLE_DB",
//        username: "SA",
//        password: "",
//        options: {maximumPoolSize:1}
//    }
//
//
//                                  int returnValue = 0;
//    string result;
//    try {
//        transaction {
//            table dt = testDB -> select("SELECT int_type, long_type from DataTable WHERE row_id = 1", null, null);
//            xml xmlResult;
//            xmlResult, _ = <xml>dt;
//            result = <string> xmlResult;
//        }
//        return result, returnValue;
//    } finally {
//        testDB -> close();
//    }
//    return "", -1;
//}
//
//function testToJsonWithinTransaction () (string, int) {
//    endpoint sql:Client testDB {
//        database: sql:DB.HSQLDB_FILE,
//        host: "./target/tempdb/",
//        port: 0,
//        name: "TEST_DATA_TABLE_DB",
//        username: "SA",
//        password: "",
//        options: {maximumPoolSize:1}
//    }
//
//
//    int returnValue = 0;
//    string result;
//    try {
//        transaction {
//            table dt = testDB -> select("SELECT int_type, long_type from DataTable WHERE row_id = 1", null, null);
//            json jsonResult;
//            jsonResult, _ = <json>dt;
//            result = jsonResult.toString();
//        }
//        return result, returnValue;
//    } finally {
//        testDB -> close();
//    }
//    return "", -2;
//}

function testGetPrimitiveTypes () (int i, int l, float f, float d, boolean b, string s) {
    endpoint sql:Client testDB {
        database: sql:DB.HSQLDB_FILE,
        host: "./target/tempdb/",
        port: 0,
        name: "TEST_DATA_TABLE_DB",
        username: "SA",
        password: "",
        options: {maximumPoolSize:1}
    };

    table dt = testDB -> select("SELECT int_type, long_type, float_type, double_type,
              boolean_type, string_type from DataTable WHERE row_id = 1", null, typeof ResultPrimitive);
    while (dt.hasNext()) {
        var rs, _ = (ResultPrimitive)dt.getNext();
        i = rs.INT_TYPE;
        l = rs.LONG_TYPE;
        f = rs.FLOAT_TYPE;
        d = rs.DOUBLE_TYPE;
        b = rs.BOOLEAN_TYPE;
        s = rs.STRING_TYPE;
    }
    testDB -> close();
    return;
}

function testGetComplexTypes () (string blobValue, string clob, string binary) {
    endpoint sql:Client testDB {
        database: sql:DB.HSQLDB_FILE,
        host: "./target/tempdb/",
        port: 0,
        name: "TEST_DATA_TABLE_DB",
        username: "SA",
        password: "",
        options: {maximumPoolSize:1}
    };

    table dt = testDB -> select("SELECT blob_type,clob_type,binary_type from ComplexTypes where row_id = 1",null,
                         typeof ResultObject);
    while (dt.hasNext()) {
        var rs, _ = (ResultObject)dt.getNext();
        blob blobData = rs.BLOB_TYPE;
        blobValue = blobData.toString("UTF-8");
        clob = rs.CLOB_TYPE;
        blob binaryData = rs.BINARY_TYPE;
        binary = binaryData.toString("UTF-8");
    }
    testDB -> close();
    return;
}

function testArrayData () (int[] int_arr, int[] long_arr, float[] float_arr, string[] string_arr, boolean[] boolean_arr) {
    endpoint sql:Client testDB {
        database: sql:DB.HSQLDB_FILE,
        host: "./target/tempdb/",
        port: 0,
        name: "TEST_DATA_TABLE_DB",
        username: "SA",
        password: "",
        options: {maximumPoolSize:1}
    };

    table dt = testDB -> select("SELECT int_array, long_array, float_array, boolean_array,
              string_array from ArrayTypes where row_id = 1", null, typeof ResultMap);
    while (dt.hasNext()) {
        var rs, _ = (ResultMap)dt.getNext();
        int_arr = rs.INT_ARRAY;
        long_arr = rs.LONG_ARRAY;
        float_arr = rs.FLOAT_ARRAY;
        boolean_arr = rs.BOOLEAN_ARRAY;
        string_arr = rs.STRING_ARRAY;
    }
    testDB -> close();
    return;
}

function testArrayDataInsertAndPrint () (int updateRet, int intArrLen, int longArrLen, int floatArrLen, int boolArrLen,
                                         int strArrLen) {
    endpoint sql:Client testDB {
        database: sql:DB.HSQLDB_FILE,
        host: "./target/tempdb/",
        port: 0,
        name: "TEST_DATA_TABLE_DB",
        username: "SA",
        password: "",
        options: {maximumPoolSize:1}
    };

    int[] dataint = [1, 2, 3];
    float[] datafloat = [33.4, 55.4];
    string[] datastring = ["hello", "world"];
    boolean[] databoolean = [true, false, false, true, true];

    sql:Parameter paraID = {sqlType:sql:Type.INTEGER, value:4};
    sql:Parameter paraInt = {sqlType:sql:Type.ARRAY, value:dataint};
    sql:Parameter paraLong = {sqlType:sql:Type.ARRAY, value:dataint};
    sql:Parameter paraFloat = {sqlType:sql:Type.ARRAY, value:datafloat};
    sql:Parameter paraString = {sqlType:sql:Type.ARRAY, value:datastring};
    sql:Parameter paraBool = {sqlType:sql:Type.ARRAY, value:databoolean};
    sql:Parameter[] parameters = [paraID, paraInt, paraLong, paraFloat, paraString, paraBool];

    updateRet = testDB -> update("insert into ArrayTypes(row_id, int_array, long_array, float_array,
                                string_array, boolean_array) values (?,?,?,?,?,?)", parameters);
    table dt = testDB -> select("SELECT int_array, long_array, float_array, boolean_array, string_array
                                 from ArrayTypes where row_id = 4", null, typeof ResultMap);
    while (dt.hasNext()) {
        var rs, _ = (ResultMap)dt.getNext();
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
    testDB -> close();
    return;
}

function testDateTime (int datein, int timein, int timestampin) (string date, string time, string timestamp,
                                                                 string datetime) {
    endpoint sql:Client testDB {
        database: sql:DB.HSQLDB_FILE,
        host: "./target/tempdb/",
        port: 0,
        name: "TEST_DATA_TABLE_DB",
        username: "SA",
        password: "",
        options: {maximumPoolSize:1}
    };

    sql:Parameter para0 = {sqlType:sql:Type.INTEGER, value:1};
    sql:Parameter para1 = {sqlType:sql:Type.DATE, value:datein};
    sql:Parameter para2 = {sqlType:sql:Type.TIME, value:timein};
    sql:Parameter para3 = {sqlType:sql:Type.TIMESTAMP, value:timestampin};
    sql:Parameter para4 = {sqlType:sql:Type.DATETIME, value:timestampin};
    sql:Parameter[] parameters = [para0, para1, para2, para3, para4];

    int insertCount = testDB -> update("Insert into DateTimeTypes
        (row_id, date_type, time_type, timestamp_type, datetime_type) values (?,?,?,?,?)", parameters);

    table dt = testDB -> select("SELECT date_type, time_type, timestamp_type, datetime_type
                from DateTimeTypes where row_id = 1", null, typeof ResultDates);
    while (dt.hasNext()) {
        var rs, _ = (ResultDates)dt.getNext();
        time = rs.TIME_TYPE;
        date = rs.DATE_TYPE;
        timestamp = rs.TIMESTAMP_TYPE;
        datetime = rs.DATETIME_TYPE;
    }
    testDB -> close();
    return;
}

function testDateTimeAsTimeStruct () (int dateInserted, int dateRetrieved, int timeInserted, int timeRetrieved,
                                      int timestampInserted, int timestampRetrieved, int datetimeInserted, int datetimeRetrieved ) {
    endpoint sql:Client testDB {
        database: sql:DB.HSQLDB_FILE,
        host: "./target/tempdb/",
        port: 0,
        name: "TEST_DATA_TABLE_DB",
        username: "SA",
        password: "",
        options: {maximumPoolSize:1}
    };

    time:Time dateStruct = time:createTime(2017, 5, 23, 0, 0, 0, 0, "");
    time:Timezone zoneValue = {zoneId:"UTC"};
    time:Time timeStruct = {time:51323000, zone:zoneValue};
    time:Time timestampStruct = time:createTime(2017, 1, 25, 16, 12, 23, 0, "UTC");
    time:Time datetimeStruct = time:createTime(2017, 1, 31, 16, 12, 23, 332, "UTC");
    dateInserted = dateStruct.time;
    timeInserted = timeStruct.time;
    timestampInserted = timestampStruct.time;
    datetimeInserted = datetimeStruct.time;

    sql:Parameter para0 = {sqlType:sql:Type.INTEGER, value:31};
    sql:Parameter para1 = {sqlType:sql:Type.DATE, value:dateStruct};
    sql:Parameter para2 = {sqlType:sql:Type.TIME, value:timeStruct};
    sql:Parameter para3 = {sqlType:sql:Type.TIMESTAMP, value:timestampStruct};
    sql:Parameter para4 = {sqlType:sql:Type.DATETIME, value:datetimeStruct};
    sql:Parameter[] parameters = [para0, para1, para2, para3, para4];

    _ = testDB -> update("Insert into DateTimeTypes
        (row_id, date_type, time_type, timestamp_type, datetime_type) values (?,?,?,?,?)", parameters);

    table dt = testDB -> select("SELECT date_type, time_type, timestamp_type, datetime_type
                from DateTimeTypes where row_id = 31", null, typeof ResultDatesStruct);
    while (dt.hasNext()) {
        var rs, _ = (ResultDatesStruct)dt.getNext();
        dateRetrieved = rs.DATE_TYPE.time;
        timeRetrieved = rs.TIME_TYPE.time;
        timestampRetrieved = rs.TIMESTAMP_TYPE.time;
        datetimeRetrieved = rs.DATETIME_TYPE.time;
    }
    testDB -> close();
    return;
}

function testDateTimeInt (int datein, int timein, int timestampin) (int  date, int time, int timestamp,
                                                                 int datetime) {
    endpoint sql:Client testDB {
        database: sql:DB.HSQLDB_FILE,
        host: "./target/tempdb/",
        port: 0,
        name: "TEST_DATA_TABLE_DB",
        username: "SA",
        password: "",
        options: {maximumPoolSize:1}
    };

    sql:Parameter para0 = {sqlType:sql:Type.INTEGER, value:32};
    sql:Parameter para1 = {sqlType:sql:Type.DATE, value:datein};
    sql:Parameter para2 = {sqlType:sql:Type.TIME, value:timein};
    sql:Parameter para3 = {sqlType:sql:Type.TIMESTAMP, value:timestampin};
    sql:Parameter para4 = {sqlType:sql:Type.DATETIME, value:timestampin};
    sql:Parameter[] parameters = [para0, para1, para2, para3, para4];

    int insertCount = testDB -> update("Insert into DateTimeTypes
        (row_id, date_type, time_type, timestamp_type, datetime_type) values (?,?,?,?,?)", parameters);

    table dt = testDB -> select("SELECT date_type, time_type, timestamp_type, datetime_type
                from DateTimeTypes where row_id = 32", null, typeof ResultDatesInt);
    while (dt.hasNext()) {
        var rs, _ = (ResultDatesInt)dt.getNext();
        time = rs.TIME_TYPE;
        date = rs.DATE_TYPE;
        timestamp = rs.TIMESTAMP_TYPE;
        datetime = rs.DATETIME_TYPE;
    }
    testDB -> close();
    return;
}

function testBlobData () (string blobStringData) {
    endpoint sql:Client testDB {
        database: sql:DB.HSQLDB_FILE,
        host: "./target/tempdb/",
        port: 0,
        name: "TEST_DATA_TABLE_DB",
        username: "SA",
        password: "",
        options: {maximumPoolSize:1}
    };

    table dt = testDB -> select("SELECT blob_type from ComplexTypes where row_id = 1", null, typeof ResultBlob);
    blob blobData;
    while (dt.hasNext()) {
        var rs, err = (ResultBlob)dt.getNext();
        blobData = rs.BLOB_TYPE;
    }
    blobStringData = blobData.toString("UTF-8");

    testDB -> close();
    return;
}

function testColumnAlias () (int i, int l, float f, float d, boolean b, string s, int i2) {
    endpoint sql:Client testDB {
        database: sql:DB.HSQLDB_FILE,
        host: "./target/tempdb/",
        port: 0,
        name: "TEST_DATA_TABLE_DB",
        username: "SA",
        password: "",
        options: {maximumPoolSize:1}
    };

    table dt = testDB -> select("SELECT dt1.int_type, dt1.long_type, dt1.float_type,
           dt1.double_type,dt1.boolean_type, dt1.string_type,dt2.int_type as dt2int_type from DataTable dt1
           left join DataTableRep dt2 on dt1.row_id = dt2.row_id WHERE dt1.row_id = 1;", null,
                                 typeof ResultSetTestAlias);
    while (dt.hasNext()) {
        var rs, err = (ResultSetTestAlias)dt.getNext();
        i = rs.INT_TYPE;
        l = rs.LONG_TYPE;
        f = rs.FLOAT_TYPE;
        d = rs.DOUBLE_TYPE;
        b = rs.BOOLEAN_TYPE;
        s = rs.STRING_TYPE;
        i2 = rs.DT2INT_TYPE;
    }
    testDB -> close();
    return;
}

function testBlobInsert () (int i) {
    endpoint sql:Client testDB {
        database: sql:DB.HSQLDB_FILE,
        host: "./target/tempdb/",
        port: 0,
        name: "TEST_DATA_TABLE_DB",
        username: "SA",
        password: "",
        options: {maximumPoolSize:1}
    };

    sql:Parameter[] params = [];
    table dt = testDB -> select("SELECT blob_type from ComplexTypes where row_id = 1", params, typeof ResultBlob);
    blob blobData;
    while (dt.hasNext()) {
        var rs, _ = (ResultBlob)dt.getNext();
        blobData = rs.BLOB_TYPE;
    }
    sql:Parameter para0 = {sqlType:sql:Type.INTEGER, value:10};
    sql:Parameter para1 = {sqlType:sql:Type.BLOB, value:blobData};
    params = [para0, para1];
    int insertCount = testDB -> update("Insert into ComplexTypes (row_id, blob_type) values (?,?)", params);
    testDB -> close();
    return insertCount;
}


function testTableAutoClose () (int i, string test) {
    endpoint sql:Client testDB {
        database: sql:DB.HSQLDB_FILE,
        host: "./target/tempdb/",
        port: 0,
        name: "TEST_DATA_TABLE_DB",
        username: "SA",
        password: "",
        options: {maximumPoolSize:1}
    };

    table dt =testDB -> select("SELECT int_type from DataTable WHERE row_id = 1", null, typeof ResultPrimitiveInt);
    while (dt.hasNext()) {
        var rs, _ = (ResultPrimitiveInt)dt.getNext();
        i = rs.INT_TYPE;
    }

    table dt2 = testDB -> select("SELECT int_type, long_type, float_type, double_type,
              boolean_type, string_type from DataTable WHERE row_id = 1", null, null);
    var jsonstring,err = <json> dt2;
    test = jsonstring.toString();

    table dt3 = testDB -> select("SELECT int_type, long_type, float_type, double_type,
              boolean_type, string_type from DataTable WHERE row_id = 1", null, null);
    testDB -> close();
    return;
}

function testTableManualClose () (int data) {
    endpoint sql:Client testDB {
        database: sql:DB.HSQLDB_FILE,
        host: "./target/tempdb/",
        port: 0,
        name: "TEST_DATA_TABLE_DB",
        username: "SA",
        password: "",
        options: {maximumPoolSize:1}
    };

    table dt = testDB -> select("SELECT int_type from DataTable", null, typeof ResultPrimitiveInt);
    int i = 0;
    while (dt.hasNext()) {
        var rs, _ = (ResultPrimitiveInt)dt.getNext();
        int ret = rs.INT_TYPE;
        i = i + 1;
        if (i == 1) {
            break;
        }
    }
    dt.close();

    table dt2 = testDB -> select("SELECT int_type from DataTable WHERE row_id = 1", null, typeof ResultPrimitiveInt);
    while (dt2.hasNext()) {
        var rs2, _ = (ResultPrimitiveInt)dt2.getNext();
        data = rs2.INT_TYPE;
    }
    dt2.close();
    testDB -> close();
    return;
}

function testCloseConnectionPool () (int count) {
    endpoint sql:Client testDB {
        database: sql:DB.HSQLDB_FILE,
        host: "./target/tempdb/",
        port: 0,
        name: "TEST_DATA_TABLE_DB",
        username: "SA",
        password: "",
        options: {maximumPoolSize:1}
    };

    table dt = testDB -> select ("SELECT COUNT(*) as countVal FROM INFORMATION_SCHEMA.SYSTEM_SESSIONS", null,
                                  typeof ResultCount);
    while (dt.hasNext()) {
        var rs, err = (ResultCount) dt.getNext();
        count = rs.COUNTVAL;
    }
    testDB -> close();
    return;
}

function testTablePrintAndPrintln() {
    endpoint sql:Client testDB {
        database: sql:DB.HSQLDB_FILE,
        host: "./target/tempdb/",
        port: 0,
        name: "TEST_DATA_TABLE_DB",
        username: "SA",
        password: "",
        options: {maximumPoolSize:1}
    };

    table dt = testDB -> select("SELECT int_type, long_type, float_type, double_type,
    boolean_type, string_type from DataTable WHERE row_id = 1", null, null);

    io:println(dt);
    io:print(dt);
    testDB -> close();
}

function testMutltipleRows () (int i1, int i2) {
    endpoint sql:Client testDB {
        database: sql:DB.HSQLDB_FILE,
        host: "./target/tempdb/",
        port: 0,
        name: "TEST_DATA_TABLE_DB",
        username: "SA",
        password: "",
        options: {maximumPoolSize:1}
    };

    table dt = testDB -> select("SELECT int_type from DataTableRep", null, typeof ResultPrimitiveInt);
    ResultPrimitiveInt rs1;
    ResultPrimitiveInt rs2;
    int i = 0;
    while (dt.hasNext()) {
        if (i == 0) {
            rs1, _ = (ResultPrimitiveInt)dt.getNext();
        } else {
            rs2, _ = (ResultPrimitiveInt)dt.getNext();
        }
        i = i + 1;
    }
    testDB -> close();
    return rs1.INT_TYPE, rs2.INT_TYPE;
}

function testMutltipleRowsWithoutLoop () (int i1, int i2, int i3, int i4, string st1, string st2) {
    endpoint sql:Client testDB {
        database: sql:DB.HSQLDB_FILE,
        host: "./target/tempdb/",
        port: 0,
        name: "TEST_DATA_TABLE_DB",
        username: "SA",
        password: "",
        options: {maximumPoolSize:1}
    };

    //Iterate the whole result
    table dt = testDB -> select("SELECT int_type from DataTableRep order by int_type desc", null,
                                 typeof ResultPrimitiveInt);
    while (dt.hasNext()) {
        var rs, _ = (ResultPrimitiveInt)dt.getNext();
        i1 = rs.INT_TYPE;
    }

    //Pick the first row only
    dt = testDB -> select("SELECT int_type from DataTableRep order by int_type desc", null, typeof ResultPrimitiveInt);
    if (dt.hasNext()) {
        var rs, _ = (ResultPrimitiveInt)dt.getNext();
        i2 = rs.INT_TYPE;
    }
    dt.close();

    //Pick all the rows without checking
    dt = testDB -> select("SELECT int_type from DataTableRep order by int_type desc", null, typeof ResultPrimitiveInt);
    var rs1, _ = (ResultPrimitiveInt)dt.getNext();
    i3 = rs1.INT_TYPE;

    var rs2, _ = (ResultPrimitiveInt)dt.getNext();
    i4 = rs2.INT_TYPE;
    dt.close();

    //Pick the first row by checking and next row without checking
    string s1 = "";
    dt = testDB -> select("SELECT int_type from DataTableRep order by int_type desc", null, typeof ResultPrimitiveInt);
    if (dt.hasNext()) {
        var rs, _ = (ResultPrimitiveInt)dt.getNext();
        int i = rs.INT_TYPE;
        s1 = s1 + i;
    }

    var rs, _ = (ResultPrimitiveInt)dt.getNext();
    int i = rs.INT_TYPE;
    s1 = s1 + "_" + i;

    if (dt.hasNext()) {
        s1 = s1 + "_" + "HAS";
    } else {
        s1 = s1 + "_" + "NOT";
    }

    //Pick the first row without checking, then check and no fetch, and finally fetch row by checking
    string s2 = "";
    dt = testDB -> select("SELECT int_type from DataTableRep order by int_type desc", null, typeof ResultPrimitiveInt);
    rs, _ = (ResultPrimitiveInt)dt.getNext();
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
        rs, _ = (ResultPrimitiveInt)dt.getNext();
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
    testDB -> close();
    return i1, i2, i3, i4, s1, s2;
}

function testHasNextWithoutConsume () (boolean b1, boolean b2, boolean b3) {
    endpoint sql:Client testDB {
        database: sql:DB.HSQLDB_FILE,
        host: "./target/tempdb/",
        port: 0,
        name: "TEST_DATA_TABLE_DB",
        username: "SA",
        password: "",
        options: {maximumPoolSize:1}
    };

    table dt = testDB -> select("SELECT int_type from DataTableRep order by int_type desc", null,
                                 typeof ResultPrimitiveInt);
    b1 = false;
    b2 = false;
    b3 = false;

    if (dt.hasNext()) {
        b1 = true;
    }
    if (dt.hasNext()) {
        b2 = true;
    }
    if (dt.hasNext()) {
        b3 = true;
    }
    testDB -> close();
    return b1, b2, b3;
}

function testGetFloatTypes () (float f, float d, float num, float dec) {
    endpoint sql:Client testDB {
        database: sql:DB.HSQLDB_FILE,
        host: "./target/tempdb/",
        port: 0,
        name: "TEST_DATA_TABLE_DB",
        username: "SA",
        password: "",
        options: {maximumPoolSize:1}
    };

    table dt = testDB -> select("SELECT float_type, double_type,
                  numeric_type, decimal_type from FloatTable WHERE row_id = 1", null, typeof ResultSetFloat);
    while (dt.hasNext()) {
        var rs, _ = (ResultSetFloat)dt.getNext();
        f = rs.FLOAT_TYPE;
        d = rs.DOUBLE_TYPE;
        num = rs.NUMERIC_TYPE;
        dec = rs.DECIMAL_TYPE;
    }
    testDB -> close();
    return;
}

function testSignedIntMaxMinValues () (int maxInsert, int minInsert, int nullInsert, string jsonStr, string xmlStr,
                                       string str) {
    endpoint sql:Client testDB {
        database: sql:DB.HSQLDB_FILE,
        host: "./target/tempdb/",
        port: 0,
        name: "TEST_DATA_TABLE_DB",
        username: "SA",
        password: "",
        options: {maximumPoolSize:1}
    };

    string insertSQL = "INSERT INTO IntegerTypes(id,tinyIntData, smallIntData, intData, bigIntData) VALUES (?,?, ?,?,?)";
    string selectSQL = "SELECT id,tinyIntData,smallIntData,intData,bigIntData FROM IntegerTypes";

    //Insert signed max
    sql:Parameter para1 = {sqlType:sql:Type.INTEGER, value:1};
    sql:Parameter para2 = {sqlType:sql:Type.TINYINT, value:127};
    sql:Parameter para3 = {sqlType:sql:Type.SMALLINT, value:32767};
    sql:Parameter para4 = {sqlType:sql:Type.INTEGER, value:2147483647};
    sql:Parameter para5 = {sqlType:sql:Type.BIGINT, value:9223372036854775807};
    sql:Parameter[] parameters = [para1, para2, para3, para4, para5];
    maxInsert = testDB -> update(insertSQL, parameters);

    //Insert signed min
    para1 = {sqlType:sql:Type.INTEGER, value:2};
    para2 = {sqlType:sql:Type.TINYINT, value:-128};
    para3 = {sqlType:sql:Type.SMALLINT, value:-32768};
    para4 = {sqlType:sql:Type.INTEGER, value:-2147483648};
    para5 = {sqlType:sql:Type.BIGINT, value:-9223372036854775808};
    parameters = [para1, para2, para3, para4, para5];
    minInsert = testDB -> update(insertSQL, parameters);

    //Insert null
    para1 = {sqlType:sql:Type.INTEGER, value:3};
    para2 = {sqlType:sql:Type.TINYINT, value:null};
    para3 = {sqlType:sql:Type.SMALLINT, value:null};
    para4 = {sqlType:sql:Type.INTEGER, value:null};
    para5 = {sqlType:sql:Type.BIGINT, value:null};
    parameters = [para1, para2, para3, para4, para5];
    nullInsert = testDB -> update(insertSQL, parameters);

    table dt = testDB -> select(selectSQL, null, null);
    var j, _ = <json>dt;
    jsonStr = j.toString();

    dt = testDB -> select(selectSQL, null, null);
    var x, _ = <xml>dt;
    xmlStr = <string>x;

    dt = testDB -> select(selectSQL, null, typeof ResultSignedInt);
    str = "";
    while (dt.hasNext()) {
        var result, _ = (ResultSignedInt)dt.getNext();
        str = str + result.ID + "|" + result.TINYINTDATA + "|" + result.SMALLINTDATA + "|" + result.INTDATA + "|" +
              result.BIGINTDATA + "#";
    }
    testDB -> close();
    return;
}

function testComplexTypeInsertAndRetrieval () (int retDataInsert, int retNullInsert, string jsonStr, string xmlStr,
                                               string str) {
    endpoint sql:Client testDB {
        database: sql:DB.HSQLDB_FILE,
        host: "./target/tempdb/",
        port: 0,
        name: "TEST_DATA_TABLE_DB",
        username: "SA",
        password: "",
        options: {maximumPoolSize:1}
    };

    string insertSQL = "INSERT INTO ComplexTypes(row_id, blob_type, clob_type, binary_type) VALUES (?,?,?,?)";
    string selectSQL = "SELECT row_id, blob_type, clob_type, binary_type FROM ComplexTypes where row_id = 100 or row_id = 200";
    string text = "Sample Text";
    blob content = text.toBlob("UTF-8");

    //Insert data
    sql:Parameter para1 = {sqlType:sql:Type.INTEGER, value:100};
    sql:Parameter para2 = {sqlType:sql:Type.BLOB, value:content};
    sql:Parameter para3 = {sqlType:sql:Type.CLOB, value:text};
    sql:Parameter para4 = {sqlType:sql:Type.BINARY, value:content};
    sql:Parameter[] parameters = [para1, para2, para3, para4];
    retDataInsert = testDB -> update(insertSQL, parameters);

    //Insert null values
    para1 = {sqlType:sql:Type.INTEGER, value:200};
    para2 = {sqlType:sql:Type.BLOB, value:null};
    para3 = {sqlType:sql:Type.CLOB, value:null};
    para4 = {sqlType:sql:Type.BINARY, value:null};
    parameters = [para1, para2, para3, para4];
    retNullInsert = testDB -> update(insertSQL, parameters);

    table dt = testDB -> select(selectSQL, null, null);
    var j,_ = <json>dt;
    jsonStr = j.toString();

    dt = testDB -> select(selectSQL, null, null);
    var x,_ = <xml>dt;
    xmlStr = <string>x;

    dt = testDB -> select(selectSQL, null, typeof ResultComplexTypes);
    str = "";
    while (dt.hasNext()) {
        var result,_ = (ResultComplexTypes)dt.getNext();
        str = str + result.ROW_ID + "|" + result.BLOB_TYPE.toString("UTF-8") + "|" + result.CLOB_TYPE + "|";
    }
    testDB -> close();
    return;
}

function testJsonXMLConversionwithDuplicateColumnNames () (string jsonStr, string xmlStr) {
    endpoint sql:Client testDB {
        database: sql:DB.HSQLDB_FILE,
        host: "./target/tempdb/",
        port: 0,
        name: "TEST_DATA_TABLE_DB",
        username: "SA",
        password: "",
        options: {maximumPoolSize:1}
    };

    table dt = testDB -> select("SELECT dt1.row_id, dt1.int_type, dt2.row_id, dt2.int_type from DataTable dt1 left
            join DataTableRep dt2 on dt1.row_id = dt2.row_id WHERE dt1.row_id = 1", null, null);
    var j,_ = <json> dt;
    jsonStr = j.toString();

    table dt2 = testDB -> select("SELECT dt1.row_id, dt1.int_type, dt2.row_id, dt2.int_type from DataTable dt1 left
            join DataTableRep dt2 on dt1.row_id = dt2.row_id WHERE dt1.row_id = 1", null, null);
    var x,_ = <xml> dt2;
    xmlStr = <string> x;

    testDB -> close();
    return;
}

function testStructFieldNotMatchingColumnName () (int countAll, int i1, int i2, int i3, int i4) {
    endpoint sql:Client testDB {
        database: sql:DB.HSQLDB_FILE,
        host: "./target/tempdb/",
        port: 0,
        name: "TEST_DATA_TABLE_DB",
        username: "SA",
        password: "",
        options: {maximumPoolSize:1}
    };

    table dt = testDB -> select("SELECT count(*) from DataTable WHERE row_id = 1", null, typeof ResultCount);
    while (dt.hasNext()) {
        var rs, _ = (ResultCount)dt.getNext();
        countAll = rs.COUNTVAL;
    }

    table dt2 = testDB -> select("SELECT dt1.row_id, dt1.int_type, dt2.row_id, dt2.int_type from DataTable dt1 left
            join DataTableRep dt2 on dt1.row_id = dt2.row_id WHERE dt1.row_id = 1", null, typeof ResultTest);
    while (dt2.hasNext()) {
        var rs, _ = (ResultTest)dt2.getNext();
        i1 = rs.t1Row;
        i2 = rs.t1Int;
        i3 = rs.t2Row;
        i4 = rs.t2Int;
    }
    testDB -> close();
    return;
}

function testGetPrimitiveTypesWithForEach () (int i, int l, float f, float d, boolean b, string s) {
    endpoint sql:Client testDB {
        database: sql:DB.HSQLDB_FILE,
        host: "./target/tempdb/",
        port: 0,
        name: "TEST_DATA_TABLE_DB",
        username: "SA",
        password: "",
        options: {maximumPoolSize:1}
    };

    table<ResultPrimitive> dt = testDB -> select("SELECT int_type, long_type, float_type, double_type,
              boolean_type, string_type from DataTable WHERE row_id = 1", null, typeof ResultPrimitive);
    foreach x in dt {
        i = x.INT_TYPE;
        l = x.LONG_TYPE;
        f = x.FLOAT_TYPE;
        d = x.DOUBLE_TYPE;
        b = x.BOOLEAN_TYPE;
        s = x.STRING_TYPE;
    }
    testDB -> close();
    return;
}

function testMutltipleRowsWithForEach () (int i1, int i2) {
    endpoint sql:Client testDB {
        database: sql:DB.HSQLDB_FILE,
        host: "./target/tempdb/",
        port: 0,
        name: "TEST_DATA_TABLE_DB",
        username: "SA",
        password: "",
        options: {maximumPoolSize:1}
    };

    table<ResultPrimitiveInt> dt = testDB -> select("SELECT int_type from DataTableRep", null, typeof ResultPrimitiveInt);
    ResultPrimitiveInt rs1;
    ResultPrimitiveInt rs2;
    int i = 0;
    foreach x in dt {
        if (i == 0) {
            rs1 = x;
        } else {
            rs2 = x;
        }
        i = i + 1;
    }
    testDB -> close();
    return rs1.INT_TYPE, rs2.INT_TYPE;
}

function testTableAddInvalid () {
    endpoint sql:Client testDB {
        database: sql:DB.HSQLDB_FILE,
        host: "./target/tempdb/",
        port: 0,
        name: "TEST_DATA_TABLE_DB",
        username: "SA",
        password: "",
        options: {maximumPoolSize:1}
    };

    table<ResultPrimitiveInt> dt = testDB -> select("SELECT int_type from DataTableRep", null, typeof ResultPrimitiveInt);
    try {
        ResultPrimitiveInt row = {INT_TYPE:443};
        dt.add(row);
    } finally {
        testDB -> close();
    }
}

function testTableRemoveInvalid () {
    endpoint sql:Client testDB {
        database: sql:DB.HSQLDB_FILE,
        host: "./target/tempdb/",
        port: 0,
        name: "TEST_DATA_TABLE_DB",
        username: "SA",
        password: "",
        options: {maximumPoolSize:1}
    };

    table<ResultPrimitiveInt> dt = testDB -> select("SELECT int_type from DataTableRep", null, typeof ResultPrimitiveInt);
    try {
        ResultPrimitiveInt row = {INT_TYPE:443};
        _ = dt.remove(isDelete);
    } finally {
        testDB -> close();
    }
}

function isDelete (ResultPrimitiveInt p) (boolean) {
    return p.INT_TYPE < 2000;
}
