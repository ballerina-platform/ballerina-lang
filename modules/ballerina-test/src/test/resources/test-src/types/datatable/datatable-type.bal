import ballerina.data.sql;

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
    map INT_ARRAY;
    map LONG_ARRAY;
    map FLOAT_ARRAY;
    map BOOLEAN_ARRAY;
    map STRING_ARRAY;
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

function testToJson () (json) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_DATA_TABLE_DB", "SA", "", {maximumPoolSize:1});
    }
    sql:Parameter[] parameters = [];

    try {
        datatable dt = testDB.select("SELECT int_type, long_type, float_type, double_type,
                  boolean_type, string_type from DataTable WHERE row_id = 1", parameters);
        json result;
        result, _ = <json>dt;
        return result;
    } finally {
        testDB.close();
    }
    return null;
}

function testToXml () (xml) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_DATA_TABLE_DB", "SA", "", {maximumPoolSize:1});
    }
    sql:Parameter[] parameters = [];

    try {
        datatable dt = testDB.select("SELECT int_type, long_type, float_type, double_type,
                   boolean_type, string_type from DataTable WHERE row_id = 1", parameters);
        xml result;
        result, _ = <xml>dt;
        return result;
    } finally {
        testDB.close();
    }
    return null;
}

function testToXmlMultipleConsume () (xml) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:HSQLDB_FILE, "./target/tempdb/",
                                   0, "TEST_DATA_TABLE_DB", "SA", "", {maximumPoolSize:1});
    }
    sql:Parameter[] parameters = [];

    try {
        datatable dt = testDB.select("SELECT int_type, long_type, float_type, double_type,
        boolean_type, string_type from DataTable WHERE row_id = 1", parameters);
        xml result;
        result, _ = <xml>dt;
        println(result);
        return result;
    } finally {
        testDB.close();
    }
    return null;
}


function toXmlComplex () (xml) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_DATA_TABLE_DB", "SA", "", {maximumPoolSize:1});
    }
    sql:Parameter[] parameters = [];

    try {
        datatable dt = testDB.select("SELECT int_type, int_array, long_type, long_array, float_type,
                    float_array, double_type, boolean_type, string_type, double_array, boolean_array, string_array
                    from MixTypes where row_id =1", parameters);
        xml result;
        result, _ = <xml>dt;
        return result;
    } finally {
        testDB.close();
    }
    return null;
}

function testJsonWithNull () (json) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_DATA_TABLE_DB", "SA", "", {maximumPoolSize:1});
    }

    sql:Parameter[] parameters = [];
    try {
        datatable dt = testDB.select("SELECT int_type, long_type, float_type, double_type,
                  boolean_type, string_type from DataTable WHERE row_id = 2", parameters);
        json result;
        result, _ = <json>dt;
        return result;
    }  finally {
        testDB.close();
    }
    return null;
}

function testXmlWithNull () (xml) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_DATA_TABLE_DB", "SA", "", {maximumPoolSize:1});
    }

    sql:Parameter[] parameters = [];
    try {
        datatable dt = testDB.select("SELECT int_type, long_type, float_type, double_type,
                   boolean_type, string_type from DataTable WHERE row_id = 2", parameters);
        xml result;
        result, _ = <xml>dt;
        return result;
    } finally {
        testDB.close();
    }
    return null;
}

function testToXmlWithinTransaction () (string, int) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_DATA_TABLE_DB", "SA", "", {maximumPoolSize:1});
    }
    int returnValue = 0;
    string result;
    try {
        transaction {
            sql:Parameter[] parameters = [];
            datatable dt = testDB.select("SELECT int_type, long_type from DataTable WHERE row_id = 1", parameters);
            xml xmlResult;
            xmlResult, _ = <xml>dt;
            result = <string> xmlResult;
        } aborted {
        returnValue = -1;
    }
    return result, returnValue;
    } finally {
        testDB.close();
    }
    return "", -1;
}

function testToJsonWithinTransaction () (string, int) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_DATA_TABLE_DB", "SA", "", {maximumPoolSize:1});
    }
    int returnValue = 0;
    string result;
    try {
        transaction {
            sql:Parameter[] parameters = [];
            datatable dt = testDB.select("SELECT int_type, long_type from DataTable WHERE row_id = 1", parameters);
            json jsonResult;
            jsonResult, _ = <json>dt;
            result = jsonResult.toString();
        } aborted {
        returnValue = -1;
    }
    return result, returnValue;
    } finally {
        testDB.close();
    }
    return "", -2;
}

function testGetPrimitiveTypes () (int i, int l, float f, float d, boolean b, string s) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_DATA_TABLE_DB", "SA", "", {maximumPoolSize:1});
    }
    sql:Parameter[] parameters = [];
    datatable dt = testDB.select("SELECT int_type, long_type, float_type, double_type,
              boolean_type, string_type from DataTable WHERE row_id = 1", parameters);
    ResultPrimitive rs;
    while (dt.hasNext()) {
        any dataStruct = dt.getNext();
        rs, _ = (ResultPrimitive)dataStruct;
        i = rs.INT_TYPE;
        l = rs.LONG_TYPE;
        f = rs.FLOAT_TYPE;
        d = rs.DOUBLE_TYPE;
        b = rs.BOOLEAN_TYPE;
        s = rs.STRING_TYPE;
    }
    testDB.close();
    return;
}

function testGetComplexTypes () (string blobValue, string clob, string binary) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_DATA_TABLE_DB", "SA", "", {maximumPoolSize:1});
    }
    sql:Parameter[] parameters = [];
    datatable dt = testDB.select("SELECT blob_type,clob_type,binary_type from ComplexTypes where row_id = 1",parameters);
    ResultObject rs;
    while (dt.hasNext()) {
        any dataStruct = dt.getNext();
        rs, _ = (ResultObject)dataStruct;

        blob blobData = rs.BLOB_TYPE;
        blobValue = blobData.toString("UTF-8");
        clob = rs.CLOB_TYPE;
        blob binaryData = rs.BINARY_TYPE;
        binary = binaryData.toString("UTF-8");
    }
    testDB.close();
    return;
}

function testArrayData () (map int_arr, map long_arr, map float_arr, map string_arr, map boolean_arr) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_DATA_TABLE_DB", "SA", "", {maximumPoolSize:1});
    }
    sql:Parameter[] parameters = [];
    datatable dt = testDB.select("SELECT int_array, long_array, float_array, boolean_array,
              string_array from ArrayTypes where row_id = 1", parameters);
    ResultMap rs;
    while (dt.hasNext()) {
        any dataStruct = dt.getNext();
        rs, _ = (ResultMap)dataStruct;

        int_arr = rs.INT_ARRAY;
        long_arr = rs.LONG_ARRAY;
        float_arr = rs.FLOAT_ARRAY;
        boolean_arr = rs.BOOLEAN_ARRAY;
        string_arr = rs.STRING_ARRAY;
    }
    testDB.close();
    return;
}

function testArrayDataInsertAndPrint () (int updateRet, int intArrLen, int longArrLen, int floatArrLen, int boolArrLen,
                                         int strArrLen) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:HSQLDB_FILE, "./target/tempdb/",
                                   0, "TEST_DATA_TABLE_DB", "SA", "", {maximumPoolSize:1});
    }
    int[] dataint = [1, 2, 3];
    float[] datafloat = [33.4, 55.4];
    string[] datastring = ["hello", "world"];
    boolean[] databoolean = [true, false, false, true, true];

    sql:Parameter paraID = {sqlType:"integer", value:4, direction:0};
    sql:Parameter paraInt = {sqlType:"array", value:dataint};
    sql:Parameter paraLong = {sqlType:"array", value:dataint};
    sql:Parameter paraFloat = {sqlType:"array", value:datafloat};
    sql:Parameter paraString = {sqlType:"array", value:datastring};
    sql:Parameter paraBool = {sqlType:"array", value:databoolean};
    sql:Parameter[] parameters = [paraID, paraInt, paraLong, paraFloat, paraString, paraBool];

    updateRet = testDB.update("insert into ArrayTypes(row_id, int_array, long_array, float_array,
                                string_array, boolean_array) values (?,?,?,?,?,?)", parameters);
    datatable dt = testDB.select("SELECT int_array, long_array, float_array, boolean_array, string_array
                                 from ArrayTypes where row_id = 4", null);
    while (dt.hasNext()) {
        var rs, _ = (ResultMap)dt.getNext();
        println(rs.INT_ARRAY);
        intArrLen = lengthof rs.INT_ARRAY;
        println(rs.LONG_ARRAY);
        longArrLen = lengthof rs.LONG_ARRAY;
        println(rs.FLOAT_ARRAY);
        floatArrLen = lengthof rs.FLOAT_ARRAY;
        println(rs.BOOLEAN_ARRAY);
        boolArrLen = lengthof rs.BOOLEAN_ARRAY;
        println(rs.STRING_ARRAY);
        strArrLen = lengthof rs.STRING_ARRAY;
    }
    testDB.close();
    return;
}

function testDateTime (int datein, int timein, int timestampin) (string date, string time, string timestamp,
                                                                 string datetime) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_DATA_TABLE_DB", "SA", "", {maximumPoolSize:1});
    }
    sql:Parameter para0 = {sqlType:"integer", value:1};
    sql:Parameter para1 = {sqlType:"DATE", value:datein};
    sql:Parameter para2 = {sqlType:"TIME", value:timein};
    sql:Parameter para3 = {sqlType:"TIMESTAMP", value:timestampin};
    sql:Parameter para4 = {sqlType:"DATETIME", value:timestampin};
    sql:Parameter[] parameters = [para0, para1, para2, para3, para4];

    int insertCount = testDB.update("Insert into DateTimeTypes
        (row_id, date_type, time_type, timestamp_type, datetime_type) values (?,?,?,?,?)", parameters);

    sql:Parameter[] emptyParam = [];
    datatable dt = testDB.select("SELECT date_type, time_type, timestamp_type, datetime_type
                from DateTimeTypes where row_id = 1", emptyParam);
    ResultDates rs;
    while (dt.hasNext()) {
        any dataStruct = dt.getNext();
        rs, _ = (ResultDates)dataStruct;
        time = rs.TIME_TYPE;
        date = rs.DATE_TYPE;
        timestamp = rs.TIMESTAMP_TYPE;
        datetime = rs.DATETIME_TYPE;
    }
    testDB.close();
    return;
}

function testBlobData () (string blobStringData) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_DATA_TABLE_DB", "SA", "", {maximumPoolSize:1});
    }

    sql:Parameter[] parameters = [];
    datatable dt = testDB.select("SELECT blob_type from ComplexTypes where row_id = 1", parameters);
    blob blobData;
    while (dt.hasNext()) {
        any dataStruct = dt.getNext();
        ResultBlob rs;
        TypeCastError err;
        rs, err = (ResultBlob)dataStruct;
        blobData = rs.BLOB_TYPE;
    }
    blobStringData = blobData.toString("UTF-8");

    testDB.close();
    return;
}

function testColumnAlias () (int i, int l, float f, float d, boolean b, string s, int i2) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_DATA_TABLE_DB", "SA", "", {maximumPoolSize:1});
    }

    sql:Parameter[] parameters = [];
    datatable dt = testDB.select("SELECT dt1.int_type, dt1.long_type, dt1.float_type,
           dt1.double_type,dt1.boolean_type, dt1.string_type,dt2.int_type as dt2int_type from DataTable dt1
           left join DataTableRep dt2 on dt1.row_id = dt2.row_id WHERE dt1.row_id = 1;", parameters);
    while (dt.hasNext()) {
        any dataStruct = dt.getNext();
        ResultSetTestAlias rs;
        TypeCastError err;
        rs, err = (ResultSetTestAlias)dataStruct;

        i = rs.INT_TYPE;
        l = rs.LONG_TYPE;
        f = rs.FLOAT_TYPE;
        d = rs.DOUBLE_TYPE;
        b = rs.BOOLEAN_TYPE;
        s = rs.STRING_TYPE;
        i2 = rs.DT2INT_TYPE;
    }
    testDB.close();
    return;
}

function testBlobInsert () (int i) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_DATA_TABLE_DB", "SA", "", {maximumPoolSize:1});
    }

    sql:Parameter[] params = [];
    datatable dt = testDB.select("SELECT blob_type from ComplexTypes where row_id = 1", params);
    blob blobData;
    while (dt.hasNext()) {
        any dataStruct = dt.getNext();
        var rs, err = (ResultBlob)dataStruct;
        blobData = rs.BLOB_TYPE;
    }
    sql:Parameter para0 = {sqlType:"integer", value:10};
    sql:Parameter para1 = {sqlType:"blob", value:blobData};
    params = [para0, para1];
    int insertCount = testDB.update("Insert into ComplexTypes (row_id, blob_type) values (?,?)", params);
    testDB.close();
    return insertCount;
}


function testDatatableAutoClose () (int i, string test) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_DATA_TABLE_DB", "SA", "", {maximumPoolSize:1});
    }

    sql:Parameter[] parameters = [];
    datatable dt =testDB.select("SELECT int_type from DataTable WHERE row_id = 1", parameters);
    ResultPrimitiveInt rs;
    while (dt.hasNext()) {
        any dataStruct = dt.getNext();
        rs, _ = (ResultPrimitiveInt)dataStruct;
        i = rs.INT_TYPE;
    }

    datatable dt2 = testDB.select("SELECT int_type, long_type, float_type, double_type,
              boolean_type, string_type from DataTable WHERE row_id = 1", parameters);
    var jsonstring,err = <json> dt2;
    test = jsonstring.toString();

    datatable dt3 = testDB.select("SELECT int_type, long_type, float_type, double_type,
              boolean_type, string_type from DataTable WHERE row_id = 1", parameters);
    testDB.close();
    return;
}

function testDatatableManualClose () (int data) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_DATA_TABLE_DB", "SA", "", {maximumPoolSize:1});
    }

    sql:Parameter[] parameters = [];
    datatable dt = testDB.select("SELECT int_type from DataTable", parameters);
    ResultPrimitiveInt rs;
    int i = 0;
    while (dt.hasNext()) {
        any dataStruct = dt.getNext();
        rs, _ = (ResultPrimitiveInt)dataStruct;
        int ret = rs.INT_TYPE;
        i = i + 1;
        if (i == 1) {
            break;
        }
    }
    dt.close();

    datatable dt2 = testDB.select("SELECT int_type from DataTable WHERE row_id = 1", parameters);
    ResultPrimitiveInt rs2;
    while (dt2.hasNext()) {
        any dataStruct = dt2.getNext();
        rs2, _ = (ResultPrimitiveInt)dataStruct;
        data = rs2.INT_TYPE;
    }
    dt2.close();
    testDB.close();
    return;
}

function testCloseConnectionPool () (int count) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_DATA_TABLE_DB", "SA", "", {maximumPoolSize:1});
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

function testPrintandPrintlnDatatable() {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_DATA_TABLE_DB", "SA", "", {maximumPoolSize:1});
    }
    sql:Parameter[] parameters = [];
    datatable dt = testDB.select("SELECT int_type, long_type, float_type, double_type,
    boolean_type, string_type from DataTable WHERE row_id = 1", parameters);

    println(dt);
    print(dt);
    testDB.close();
}
function testMutltipleRows () (int i1, int i2) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:HSQLDB_FILE, "./target/tempdb/",
                                          0, "TEST_DATA_TABLE_DB", "SA", "", {maximumPoolSize:1});
    }

    sql:Parameter[] parameters = [];
    datatable dt = testDB.select("SELECT int_type from DataTableRep", parameters);
    ResultPrimitiveInt rs1;
    ResultPrimitiveInt rs2;
    int i = 0;
    while (dt.hasNext()) {
        any dataStruct = dt.getNext();
        if (i == 0) {
            rs1, _ = (ResultPrimitiveInt)dataStruct;
        } else {
            rs2, _ = (ResultPrimitiveInt)dataStruct;
        }
        i = i +1;
    }
    testDB.close();
    return rs1.INT_TYPE, rs2.INT_TYPE;
}

function testGetFloatTypes () (float f, float d, float num, float dec) {
    endpoint<sql:ClientConnector> testDB {
                                  create sql:ClientConnector(sql:HSQLDB_FILE, "./target/tempdb/",
                                                             0, "TEST_DATA_TABLE_DB", "SA", "", {maximumPoolSize:1});
    }
    datatable dt = testDB.select("SELECT float_type, double_type,
                  numeric_type, decimal_type from FloatTable WHERE row_id = 1", null);
    ResultSetFloat rs;
    while (dt.hasNext()) {
        rs, _ = (ResultSetFloat)dt.getNext();
        f = rs.FLOAT_TYPE;
        d = rs.DOUBLE_TYPE;
        num = rs.NUMERIC_TYPE;
        dec = rs.DECIMAL_TYPE;
    }
    testDB.close();
    return;
}

function testSignedIntMaxMinValues () (int maxInsert, int minInsert, int nullInsert, string jsonStr, string xmlStr,
                                       string str) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:HSQLDB_FILE, "./target/tempdb/",
                                   0, "TEST_DATA_TABLE_DB", "SA", "", {maximumPoolSize:3});
    }
    string insertSQL = "INSERT INTO IntegerTypes(id,tinyIntData, smallIntData, intData, bigIntData) VALUES (?,?, ?,?,?)";
    string selectSQL = "SELECT id,tinyIntData,smallIntData,intData,bigIntData FROM IntegerTypes";

    //Insert signed max
    sql:Parameter para1 = {sqlType:"INTEGER", value:1, direction:0};
    sql:Parameter para2 = {sqlType:"TINYINT", value:127, direction:0};
    sql:Parameter para3 = {sqlType:"SMALLINT", value:32767, direction:0};
    sql:Parameter para4 = {sqlType:"INTEGER", value:2147483647, direction:0};
    sql:Parameter para5 = {sqlType:"BIGINT", value:9223372036854775807, direction:0};
    sql:Parameter[] parameters = [para1, para2, para3, para4, para5];
    maxInsert = testDB.update(insertSQL, parameters);

    //Insert signed min
    para1 = {sqlType:"INTEGER", value:2, direction:0};
    para2 = {sqlType:"TINYINT", value:-128, direction:0};
    para3 = {sqlType:"SMALLINT", value:-32768, direction:0};
    para4 = {sqlType:"INTEGER", value:-2147483648, direction:0};
    para5 = {sqlType:"BIGINT", value:-9223372036854775808, direction:0};
    parameters = [para1, para2, para3, para4, para5];
    minInsert = testDB.update(insertSQL, parameters);

    //Insert null
    para1 = {sqlType:"INTEGER", value:3, direction:0};
    para2 = {sqlType:"TINYINT", value:null, direction:0};
    para3 = {sqlType:"SMALLINT", value:null, direction:0};
    para4 = {sqlType:"INTEGER", value:null, direction:0};
    para5 = {sqlType:"BIGINT", value:null, direction:0};
    parameters = [para1, para2, para3, para4, para5];
    nullInsert = testDB.update(insertSQL, parameters);

    datatable dt = testDB.select(selectSQL, null);
    var j, _ = <json>dt;
    jsonStr = j.toString();

    dt = testDB.select(selectSQL, null);
    var x, _ = <xml>dt;
    xmlStr = <string>x;

    dt = testDB.select(selectSQL, null);
    while (dt.hasNext()) {
        var result, _ = (ResultSignedInt)dt.getNext();
        str = str + result.ID + "|" + result.TINYINTDATA + "|" + result.SMALLINTDATA + "|" + result.INTDATA + "|" +
              result.BIGINTDATA + "#";
    }
    testDB.close();
    return;
}

function testComplexTypeInsertAndRetrieval () (int retDataInsert, int retNullInsert, string jsonStr, string xmlStr, string str) {
    endpoint<sql:ClientConnector> testDB {
        create sql:ClientConnector(sql:HSQLDB_FILE, "./target/tempdb/",
                                   0, "TEST_DATA_TABLE_DB", "SA", "", {maximumPoolSize:3});
    }

    string insertSQL = "INSERT INTO ComplexTypes(row_id, blob_type, clob_type, binary_type) VALUES (?,?,?,?)";
    string selectSQL = "SELECT row_id, blob_type, clob_type, binary_type FROM ComplexTypes where row_id = 100 or row_id = 200";
    string text = "Sample Text";
    blob content = text.toBlob("UTF-8");

    //Insert data
    sql:Parameter para1 = {sqlType:"INTEGER", value:100};
    sql:Parameter para2 = {sqlType:"BLOB", value:content};
    sql:Parameter para3 = {sqlType:"CLOB", value:text};
    sql:Parameter para4 = {sqlType:"BINARY", value:content};
    sql:Parameter[] parameters = [para1, para2, para3, para4];
    retDataInsert = testDB.update(insertSQL, parameters);

    //Insert null values
    para1 = {sqlType:"INTEGER", value:200};
    para2 = {sqlType:"BLOB", value:null};
    para3 = {sqlType:"CLOB", value:null};
    para4 = {sqlType:"BINARY", value:null};
    parameters = [para1, para2, para3, para4];
    retNullInsert = testDB.update(insertSQL, parameters);

    datatable dt = testDB.select(selectSQL, null);
    var j,_ = <json>dt;
    jsonStr = j.toString();

    dt = testDB.select(selectSQL, null);
    var x,_ = <xml>dt;
    xmlStr = <string>x;

    dt = testDB.select(selectSQL, null);
    while (dt.hasNext()) {
        var result,_ = (ResultComplexTypes)dt.getNext();
        str = str + result.ROW_ID + "|" + result.BLOB_TYPE.toString("UTF-8") + "|" + result.CLOB_TYPE + "|";
    }
    testDB.close();
    return;
}
