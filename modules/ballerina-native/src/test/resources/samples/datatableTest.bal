import ballerina.lang.datatables;
import ballerina.data.sql;
import ballerina.lang.errors;
import ballerina.lang.xmls;
import ballerina.lang.jsons;
import ballerina.lang.blobs;

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
    string TIME_TYPE;
    string DATE_TYPE;
    string TIMESTAMP_TYPE;
    string DATETIME_TYPE;
    string BINARY_TYPE;
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


function getXXXByIndex () (int i, int l, float f, float d, boolean b, string s) {
    map propertiesMap = {"jdbcUrl":"jdbc:hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
                        "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);

    sql:Parameter[] parameters = [];
    datatable df = sql:ClientConnector.select (testDB, "SELECT int_type, long_type, float_type, double_type,
              boolean_type, string_type from DataTable WHERE row_id = 1", parameters);
    while (datatables:hasNext(df)) {
        i = datatables:getInt(df, 1);
        l = datatables:getInt(df, 2);
        f = datatables:getFloat(df, 3);
        d = datatables:getFloat(df, 4);
        b = datatables:getBoolean(df, 5);
        s = datatables:getString(df, 6);
    }
    datatables:close(df);
    sql:ClientConnector.close (testDB);
    return;
}

function getXXXByName () (int i, int l, float f, float d, boolean b, string s) {
    map propertiesMap = {"jdbcUrl":"jdbc:hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
                        "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);

    sql:Parameter[] parameters = [];
    datatable df = sql:ClientConnector.select (testDB, "SELECT int_type, long_type, float_type, double_type,
              boolean_type, string_type from DataTable WHERE row_id = 1", parameters);
    while (datatables:hasNext(df)) {
        i = datatables:getInt(df, "int_type");
        l = datatables:getInt(df, "long_type");
        f = datatables:getFloat(df, "float_type");
        d = datatables:getFloat(df, "double_type");
        b = datatables:getBoolean(df, "boolean_type");
        s = datatables:getString(df, "string_type");
    }
    datatables:close(df);
    sql:ClientConnector.close (testDB);
    return;
}

function toJson () (json) {
    map propertiesMap = {"jdbcUrl":"jdbc:hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
                        "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);
    sql:Parameter[] parameters = [];

    datatable df = sql:ClientConnector.select (testDB, "SELECT int_type, long_type, float_type, double_type,
              boolean_type, string_type from DataTable WHERE row_id = 1", parameters);
    json result = datatables:toJson(df);
    return result;
}

function toXmlWithWrapper () (xml) {
    map propertiesMap = {"jdbcUrl":"jdbc:hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
                        "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);
    sql:Parameter[] parameters = [];

    datatable df = sql:ClientConnector.select (testDB, "SELECT int_type, long_type, float_type, double_type,
               boolean_type, string_type from DataTable WHERE row_id = 1", parameters);
    xml result = datatables:toXml(df, "types", "type");
    return result;
}

function toXmlComplex () (xml) {
    map propertiesMap = {"jdbcUrl":"jdbc:hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
                        "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);
    sql:Parameter[] parameters = [];

    datatable df = sql:ClientConnector.select (testDB, "SELECT int_type, int_array, long_type, long_array, float_type,
                float_array, double_type, boolean_type, string_type, double_array, boolean_array, string_array
                from MixTypes LIMIT 1", parameters);
    xml result = datatables:toXml(df, "types", "type");
    return result;
}

function getByName () (string blobValue, string clob, int time, int date, int timestamp) {
    map propertiesMap = {"jdbcUrl":"jdbc:hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
                        "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);

    sql:Parameter[] parameters = [];
    datatable df = sql:ClientConnector.select (testDB, "SELECT blob_type, clob_type, time_type, date_type,
                timestamp_type from ComplexTypes LIMIT 1", parameters);
    while (datatables:hasNext(df)) {
        blobValue = datatables:getStringWithType(df, "blob_type", "blob");
        clob = datatables:getStringWithType(df, "clob_type", "clob");
        time = datatables:getIntWithType(df, "time_type", "time");
        date = datatables:getIntWithType(df, "date_type", "date");
        timestamp = datatables:getIntWithType(df, "timestamp_type", "timestamp");
    }
    datatables:close(df);
    sql:ClientConnector.close (testDB);
    return;
}

function getByIndex () (string blobValue, string clob, int time, int date, int timestamp, string binary) {
    map propertiesMap = {"jdbcUrl":"jdbc:hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
                        "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);

    sql:Parameter[] parameters = [];
    sql:ClientConnector.update (testDB, "Update ComplexTypes set clob_type = 'Test String' where row_id = 1", parameters);
    datatable df = sql:ClientConnector.select (testDB, "SELECT blob_type, clob_type, time_type, date_type,
          timestamp_type, binary_type from ComplexTypes LIMIT 1", parameters);
    while (datatables:hasNext(df)) {
        blobValue = datatables:getStringWithType(df, 1, "blob");
        clob = datatables:getStringWithType(df, 2, "clob");
        time = datatables:getIntWithType(df, 3, "time");
        date = datatables:getIntWithType(df, 4, "date");
        timestamp = datatables:getIntWithType(df, 5, "timestamp");
        binary = datatables:getStringWithType(df, 6, "binary");
    }
    datatables:close(df);
    sql:ClientConnector.close (testDB);
    return;
}

function getObjectAsStringByIndex () (string blobValue, string clob, string time, string date, string timestamp,
                                      string datetime) {
    map propertiesMap = {"jdbcUrl":"jdbc:hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
                        "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);

    sql:Parameter[] parameters = [];
    datatable df = sql:ClientConnector.select (testDB, "SELECT blob_type, clob_type, time_type, date_type,
              timestamp_type, datetime_type from ComplexTypes LIMIT 1", parameters);
    while (datatables:hasNext(df)) {
        blobValue = datatables:getValueAsString(df, 1);
        clob = datatables:getValueAsString(df, 2);
        time = datatables:getValueAsString(df, 3);
        date = datatables:getValueAsString(df, 4);
        timestamp = datatables:getValueAsString(df, 5);
        datetime = datatables:getValueAsString(df, 6);
    }
    datatables:close(df);
    sql:ClientConnector.close (testDB);
    return;
}

function getObjectAsStringByName () (string blobValue, string clob, string time, string date, string timestamp,
                                     string datetime) {
    map propertiesMap = {"jdbcUrl":"jdbc:hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
                        "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);

    sql:Parameter[] parameters = [];
    datatable df = sql:ClientConnector.select (testDB, "SELECT blob_type, clob_type, time_type, date_type,
              timestamp_type, datetime_type from ComplexTypes LIMIT 1", parameters);
    while (datatables:hasNext(df)) {
        blobValue = datatables:getValueAsString(df, "blob_type");
        clob = datatables:getValueAsString(df, "clob_type");
        time = datatables:getValueAsString(df, "time_type");
        date = datatables:getValueAsString(df, "date_type");
        timestamp = datatables:getValueAsString(df, "timestamp_type");
        datetime = datatables:getValueAsString(df, "datetime_type");
    }
    datatables:close(df);
    sql:ClientConnector.close (testDB);
    return;
}


function getArrayByName () (map int_arr, map long_arr, map float_arr, map string_arr, map boolean_arr) {
    map propertiesMap = {"jdbcUrl":"jdbc:hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
                        "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);

    sql:Parameter[] parameters = [];
    datatable df = sql:ClientConnector.select (testDB, "SELECT int_array, long_array, float_array, boolean_array,
              string_array from ArrayTypes LIMIT 1", parameters);
    while (datatables:hasNext(df)) {
        int_arr = datatables:getArray(df, "int_array");
        long_arr = datatables:getArray(df, "long_array");
        float_arr = datatables:getArray(df, "float_array");
        boolean_arr = datatables:getArray(df, "boolean_array");
        string_arr = datatables:getArray(df, "string_array");
    }
    datatables:close(df);
    sql:ClientConnector.close (testDB);
    return;
}

function getArrayByIndex () (map int_arr, map long_arr, map float_arr, map string_arr, map boolean_arr) {
    map propertiesMap = {"jdbcUrl":"jdbc:hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
                        "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);

    sql:Parameter[] parameters = [];
    datatable df = sql:ClientConnector.select (testDB, "SELECT int_array, long_array, float_array, boolean_array,
              string_array from ArrayTypes LIMIT 1", parameters);
    while (datatables:hasNext(df)) {
        int_arr = datatables:getArray(df, 1);
        long_arr = datatables:getArray(df, 2);
        float_arr = datatables:getArray(df, 3);
        boolean_arr = datatables:getArray(df, 4);
        string_arr = datatables:getArray(df, 5);
    }
    datatables:close(df);
    sql:ClientConnector.close (testDB);
    return;
}

function testDateTime (int time, int date, int timestamp) (int time1, int date1, int timestamp1) {
    map propertiesMap = {"jdbcUrl":"jdbc:hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
                        "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);

    sql:Parameter para1 = {sqlType:"TIME", value:time, direction:0};
    sql:Parameter para2 = {sqlType:"DATE", value:date, direction:0};
    sql:Parameter para3 = {sqlType:"TIMESTAMP", value:timestamp, direction:0};
    sql:Parameter[] parameters = [para1, para2, para3];

    int insertCount = sql:ClientConnector.update (testDB, "Insert into DateTimeTypes
        (time_type, date_type, timestamp_type) values (?,?,?)", parameters);

    sql:Parameter[] emptyParam = [];
    datatable dt = sql:ClientConnector.select (testDB, "SELECT time_type, date_type, timestamp_type
                from DateTimeTypes LIMIT 1", emptyParam);
    while (datatables:hasNext(dt)) {
        time1 = datatables:getIntWithType(dt, "time_type", "time");
        date1 = datatables:getIntWithType(dt, "date_type", "date");
        timestamp1 = datatables:getIntWithType(dt, "timestamp_type", "timestamp");
    }
    datatables:close(dt);
    sql:ClientConnector.close (testDB);
    return;
}

function testJsonWithNull () (json) {
    map propertiesMap = {"jdbcUrl":"jdbc:hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
                        "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);

    sql:Parameter[] parameters = [];
    datatable df = sql:ClientConnector.select (testDB, "SELECT int_type, long_type, float_type, double_type,
              boolean_type, string_type from DataTable WHERE row_id = 2", parameters);
    json result = datatables:toJson(df);
    return result;
}

function testXmlWithNull () (xml) {
    map propertiesMap = {"jdbcUrl":"jdbc:hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
                        "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);

    sql:Parameter[] parameters = [];
    datatable df = sql:ClientConnector.select (testDB, "SELECT int_type, long_type, float_type, double_type,
               boolean_type, string_type from DataTable WHERE row_id = 2", parameters);
    xml result = datatables:toXml(df, "types", "type");
    return result;
}

function getXXXByIndexWithStruct () (int i, int l, float f, float d, boolean b, string s) {
    map propertiesMap = {"jdbcUrl":"jdbc:hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
                        "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);

    sql:Parameter[] parameters = [];
    datatable df = sql:ClientConnector.select (testDB, "SELECT int_type, long_type, float_type, double_type,
               boolean_type, string_type from DataTable WHERE row_id = 1", parameters);
    while (datatables:hasNext(df)) {
        any para = datatables:next(df);
        ResultPrimitive rs;
        errors:TypeCastError err;
        rs, err = (ResultPrimitive)para;

        i = rs.INT_TYPE;
        l = rs.LONG_TYPE;
        f = rs.FLOAT_TYPE;
        d = rs.DOUBLE_TYPE;
        b = rs.BOOLEAN_TYPE;
        s = rs.STRING_TYPE;
    }
    datatables:close(df);
    sql:ClientConnector.close (testDB);
    return;
}

function getObjectAsStringByNameWithStruct () (string blobValue, string clob, string time, string date,
                                               string timestamp, string datetime, string binary) {
    map propertiesMap = {"jdbcUrl":"jdbc:hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
                        "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);

    sql:Parameter[] parameters = [];
    datatable df = sql:ClientConnector.select (testDB, "SELECT blob_type, clob_type, time_type, date_type,
              timestamp_type, datetime_type, binary_type from ComplexTypes LIMIT 1", parameters);
    while (datatables:hasNext(df)) {
        any para = datatables:next(df);
        ResultObject rs;
        errors:TypeCastError err;
        rs, err = (ResultObject)para;

        blob blobData = rs.BLOB_TYPE;
        blobValue  = blobs:toString(blobData, "UTF-8");
        clob = rs.CLOB_TYPE;
        time = rs.TIME_TYPE;
        date = rs.DATE_TYPE;
        timestamp = rs.TIMESTAMP_TYPE;
        datetime = rs.DATETIME_TYPE;
        binary = rs.BINARY_TYPE;
    }
    datatables:close(df);
    sql:ClientConnector.close (testDB);
    return;
}

function testGetArrayByNameWithStruct () (map int_arr, map long_arr, map float_arr, map string_arr, map boolean_arr) {
    map propertiesMap = {"jdbcUrl":"jdbc:hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
                        "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);

    sql:Parameter[] parameters = [];
    datatable df = sql:ClientConnector.select (testDB, "SELECT int_array, long_array, float_array, boolean_array,
              string_array from ArrayTypes LIMIT 1", parameters);
    while (datatables:hasNext(df)) {
        any para = datatables:next(df);
        ResultMap rs;
        errors:TypeCastError err;
        rs, err = (ResultMap)para;

        int_arr = rs.INT_ARRAY;
        long_arr = rs.LONG_ARRAY;
        float_arr = rs.FLOAT_ARRAY;
        boolean_arr = rs.BOOLEAN_ARRAY;
        string_arr = rs.STRING_ARRAY;
    }
    datatables:close(df);
    sql:ClientConnector.close (testDB);
    return;
}

function testtoJsonWithStruct () (json) {
    map propertiesMap = {"jdbcUrl":"jdbc:hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
                        "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);

    sql:Parameter[] parameters = [];
    datatable df = sql:ClientConnector.select (testDB, "SELECT int_type, long_type, float_type, double_type,
              boolean_type, string_type from DataTable WHERE row_id = 1", parameters);
    json result = < json > df;
    return result;
}

function testToXmlWithStruct () (xml) {
    map propertiesMap = {"jdbcUrl":"jdbc:hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
                        "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);

    sql:Parameter[] parameters = [];
    datatable df = sql:ClientConnector.select (testDB, "SELECT int_type, long_type, float_type, double_type,
              boolean_type, string_type from DataTable WHERE row_id = 1", parameters);
    xml result = < xml > df;
    return result;
}


function testToXmlWithinTransaction () (string, int){
    map propertiesMap = {"jdbcUrl":"jdbc:hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
                        "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);
    int returnValue = 0;
    string result;
    try {
        transaction {
            sql:Parameter[] parameters = [];
            datatable df = sql:ClientConnector.select (testDB, "SELECT int_type, long_type from DataTable
                WHERE row_id = 1", parameters);
            xml xmlResult = < xml > df;
            result = xmls:toString(xmlResult);
        }aborted {
            returnValue = - 1;
        }
    } catch (errors:Error ex) {
        returnValue = - 2;
    }
    return result, returnValue;
}

function testToJsonWithinTransaction () (string, int){
    map propertiesMap = {"jdbcUrl":"jdbc:hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
                        "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);
    int returnValue = 0;
    string result;
    try {
        transaction {
            sql:Parameter[] parameters = [];
            datatable df = sql:ClientConnector.select (testDB, "SELECT int_type, long_type from DataTable
                WHERE row_id = 1", parameters);
            json jsonResult = < json > df;
            result = jsons:toString(jsonResult);
        }aborted {
            returnValue = - 1;
        }
    } catch (errors:Error ex) {
        returnValue = - 2;
    }
    return result, returnValue;
}

function testBlobData () (string blobStringData) {
    map propertiesMap = {"jdbcUrl":"jdbc:hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
                        "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);

    sql:Parameter[] parameters=[];
    datatable df2 = sql:ClientConnector.select (testDB, "SELECT blob_type from ComplexTypes LIMIT 1", parameters);
    blob blobData;
    while (datatables:hasNext(df2)) {
        any para = datatables:next(df2);
        ResultBlob rs;
        errors:TypeCastError err;
        rs, err = (ResultBlob)para;
        blobData = rs.BLOB_TYPE;
    }
    blobStringData  = blobs:toString(blobData, "UTF-8");

    sql:ClientConnector.close (testDB);
    return;
}

function getXXXByIndexWithStructAlias () (int i, int l, float f, float d, boolean b, string s, int i2) {
    map propertiesMap = {"jdbcUrl":"jdbc:hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
                        "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);

    sql:Parameter[] parameters = [];
    datatable df = sql:ClientConnector.select (testDB, "SELECT dt1.int_type, dt1.long_type, dt1.float_type,
           dt1.double_type,dt1.boolean_type, dt1.string_type,dt2.int_type as dt2int_type from DataTable dt1
           left join DataTableRep dt2 on dt1.row_id = dt2.row_id WHERE dt1.row_id = 1;", parameters);
    while (datatables:hasNext(df)) {
        any para = datatables:next(df);
        ResultSetTestAlias rs;
        errors:TypeCastError err;
        rs, err = (ResultSetTestAlias)para;

        i = rs.INT_TYPE;
        l = rs.LONG_TYPE;
        f = rs.FLOAT_TYPE;
        d = rs.DOUBLE_TYPE;
        b = rs.BOOLEAN_TYPE;
        s = rs.STRING_TYPE;
        i2 = rs.DT2INT_TYPE;
    }
    datatables:close(df);
    sql:ClientConnector.close (testDB);
    return;
}