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

struct ResultDates {
    string DATE_TYPE;
    string TIME_TYPE;
    string TIMESTAMP_TYPE;
    string DATETIME_TYPE;
}


function testGetPrimitiveTypes () (int i, int l, float f, float d, boolean b, string s) {
    map propertiesMap = {"jdbcUrl":"jdbc:hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
                            "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);

    sql:Parameter[] parameters = [];
    datatable dt = sql:ClientConnector.select(testDB, "SELECT int_type, long_type, float_type, double_type,
              boolean_type, string_type from DataTable WHERE row_id = 1", parameters);
    ResultPrimitive rs;
    while (datatables:hasNext(dt)) {
        any dataStruct = datatables:next(dt);
        rs, _ = (ResultPrimitive)dataStruct;
        i = rs.INT_TYPE;
        l = rs.LONG_TYPE;
        f = rs.FLOAT_TYPE;
        d = rs.DOUBLE_TYPE;
        b = rs.BOOLEAN_TYPE;
        s = rs.STRING_TYPE;
    }
    datatables:close(dt);
    sql:ClientConnector.close(testDB);
    return;
}

function testToJson () (json) {
    map propertiesMap = {"jdbcUrl":"jdbc:hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
                            "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);
    sql:Parameter[] parameters = [];

    datatable dt = sql:ClientConnector.select(testDB, "SELECT int_type, long_type, float_type, double_type,
              boolean_type, string_type from DataTable WHERE row_id = 1", parameters);
    json result = <json>dt;
    return result;
}

function testToXml () (xml) {
    map propertiesMap = {"jdbcUrl":"jdbc:hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
                            "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);
    sql:Parameter[] parameters = [];

    datatable dt = sql:ClientConnector.select(testDB, "SELECT int_type, long_type, float_type, double_type,
               boolean_type, string_type from DataTable WHERE row_id = 1", parameters);
    xml result = <xml>dt;
    return result;
}

function toXmlComplex () (xml) {
    map propertiesMap = {"jdbcUrl":"jdbc:hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
                            "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);
    sql:Parameter[] parameters = [];

    datatable dt = sql:ClientConnector.select(testDB, "SELECT int_type, int_array, long_type, long_array, float_type,
                float_array, double_type, boolean_type, string_type, double_array, boolean_array, string_array
                from MixTypes where row_id =1", parameters);
    xml result = <xml>dt;
    return result;
}

function testDateTime (int datein, int timein, int timestampin) (string date, string time, string timestamp,
                                                                 string datetime) {
    map propertiesMap = {"jdbcUrl":"jdbc:hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
                            "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);

    sql:Parameter para0 = {sqlType:"integer", value:1};
    sql:Parameter para1 = {sqlType:"DATE", value:datein};
    sql:Parameter para2 = {sqlType:"TIME", value:timein};
    sql:Parameter para3 = {sqlType:"TIMESTAMP", value:timestampin};
    sql:Parameter para4 = {sqlType:"DATETIME", value:timestampin};
    sql:Parameter[] parameters = [para0, para1, para2, para3, para4];

    int insertCount = sql:ClientConnector.update(testDB, "Insert into DateTimeTypes
        (row_id, date_type, time_type, timestamp_type, datetime_type) values (?,?,?,?,?)", parameters);

    sql:Parameter[] emptyParam = [];
    datatable dt = sql:ClientConnector.select(testDB, "SELECT date_type, time_type, timestamp_type, datetime_type
                from DateTimeTypes where row_id = 1", emptyParam);
    ResultDates rs;
    while (datatables:hasNext(dt)) {
        any dataStruct = datatables:next(dt);
        rs, _ = (ResultDates)dataStruct;
        time = rs.TIME_TYPE;
        date = rs.DATE_TYPE;
        timestamp = rs.TIMESTAMP_TYPE;
        datetime = rs.DATETIME_TYPE;
    }
    datatables:close(dt);
    sql:ClientConnector.close(testDB);
    return;
}

function testGetComplexTypes () (string blobValue, string clob, string time, string date, string timestamp,
                                 string datetime, string binary) {
    map propertiesMap = {"jdbcUrl":"jdbc:hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
                            "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);

    sql:Parameter[] parameters = [];
    datatable dt = sql:ClientConnector.select(testDB, "SELECT blob_type, clob_type, time_type, date_type,
              timestamp_type, datetime_type, binary_type from ComplexTypes where row_id = 1", parameters);
    ResultObject rs;
    while (datatables:hasNext(dt)) {
        any dataStruct = datatables:next(dt);
        rs, _ = (ResultObject)dataStruct;

        blob blobData = rs.BLOB_TYPE;
        blobValue = blobs:toString(blobData, "UTF-8");
        clob = rs.CLOB_TYPE;
        time = rs.TIME_TYPE;
        date = rs.DATE_TYPE;
        timestamp = rs.TIMESTAMP_TYPE;
        datetime = rs.DATETIME_TYPE;
        binary = rs.BINARY_TYPE;
    }
    datatables:close(dt);
    sql:ClientConnector.close(testDB);
    return;
}


function testArrayData () (map int_arr, map long_arr, map float_arr, map string_arr, map boolean_arr) {
    map propertiesMap = {"jdbcUrl":"jdbc:hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
                            "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);

    sql:Parameter[] parameters = [];
    datatable dt = sql:ClientConnector.select(testDB, "SELECT int_array, long_array, float_array, boolean_array,
              string_array from ArrayTypes where row_id = 1", parameters);
    ResultMap rs;
    while (datatables:hasNext(dt)) {
        any dataStruct = datatables:next(dt);
        rs, _ = (ResultMap)dataStruct;

        int_arr = rs.INT_ARRAY;
        long_arr = rs.LONG_ARRAY;
        float_arr = rs.FLOAT_ARRAY;
        boolean_arr = rs.BOOLEAN_ARRAY;
        string_arr = rs.STRING_ARRAY;
    }
    datatables:close(dt);
    sql:ClientConnector.close(testDB);
    return;
}

function testJsonWithNull () (json) {
    map propertiesMap = {"jdbcUrl":"jdbc:hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
                            "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);

    sql:Parameter[] parameters = [];
    datatable dt = sql:ClientConnector.select(testDB, "SELECT int_type, long_type, float_type, double_type,
              boolean_type, string_type from DataTable WHERE row_id = 2", parameters);
    json result = <json>dt;
    return result;
}

function testXmlWithNull () (xml) {
    map propertiesMap = {"jdbcUrl":"jdbc:hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
                            "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);

    sql:Parameter[] parameters = [];
    datatable dt = sql:ClientConnector.select(testDB, "SELECT int_type, long_type, float_type, double_type,
               boolean_type, string_type from DataTable WHERE row_id = 2", parameters);
    xml result = <xml>dt;
    return result;
}

function testToXmlWithinTransaction () (string, int) {
    map propertiesMap = {"jdbcUrl":"jdbc:hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
                            "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);
    int returnValue = 0;
    string result;
    try {
        transaction {
            sql:Parameter[] parameters = [];
            datatable dt = sql:ClientConnector.select(testDB, "SELECT int_type, long_type from DataTable
                WHERE row_id = 1", parameters);
            xml xmlResult = <xml>dt;
            result = xmls:toString(xmlResult);
        } aborted {
            returnValue = -1;
        }
    } catch (errors:Error ex) {
        returnValue = -2;
    }
    return result, returnValue;
}

function testToJsonWithinTransaction () (string, int) {
    map propertiesMap = {"jdbcUrl":"jdbc:hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
                            "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);
    int returnValue = 0;
    string result;
    try {
        transaction {
            sql:Parameter[] parameters = [];
            datatable dt = sql:ClientConnector.select(testDB, "SELECT int_type, long_type from DataTable
                WHERE row_id = 1", parameters);
            json jsonResult = <json>dt;
            result = jsons:toString(jsonResult);
        } aborted {
            returnValue = -1;
        }
    } catch (errors:Error ex) {
        returnValue = -2;
    }
    return result, returnValue;
}

function testBlobData () (string blobStringData) {
    map propertiesMap = {"jdbcUrl":"jdbc:hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
                            "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);

    sql:Parameter[] parameters = [];
    datatable dt = sql:ClientConnector.select(testDB, "SELECT blob_type from ComplexTypes where row_id = 1", parameters);
    blob blobData;
    while (datatables:hasNext(dt)) {
        any dataStruct = datatables:next(dt);
        ResultBlob rs;
        errors:TypeCastError err;
        rs, err = (ResultBlob)dataStruct;
        blobData = rs.BLOB_TYPE;
    }
    blobStringData = blobs:toString(blobData, "UTF-8");

    sql:ClientConnector.close(testDB);
    return;
}

function testColumnAlias () (int i, int l, float f, float d, boolean b, string s, int i2) {
    map propertiesMap = {"jdbcUrl":"jdbc:hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
                            "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);

    sql:Parameter[] parameters = [];
    datatable dt = sql:ClientConnector.select(testDB, "SELECT dt1.int_type, dt1.long_type, dt1.float_type,
           dt1.double_type,dt1.boolean_type, dt1.string_type,dt2.int_type as dt2int_type from DataTable dt1
           left join DataTableRep dt2 on dt1.row_id = dt2.row_id WHERE dt1.row_id = 1;", parameters);
    while (datatables:hasNext(dt)) {
        any dataStruct = datatables:next(dt);
        ResultSetTestAlias rs;
        errors:TypeCastError err;
        rs, err = (ResultSetTestAlias)dataStruct;

        i = rs.INT_TYPE;
        l = rs.LONG_TYPE;
        f = rs.FLOAT_TYPE;
        d = rs.DOUBLE_TYPE;
        b = rs.BOOLEAN_TYPE;
        s = rs.STRING_TYPE;
        i2 = rs.DT2INT_TYPE;
    }
    datatables:close(dt);
    sql:ClientConnector.close(testDB);
    return;
}