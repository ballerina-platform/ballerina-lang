import ballerina.lang.datatables;
import ballerina.data.sql;

function getXXXByIndex()(int, long, float, double, boolean, string) {
    map propertiesMap = {"jdbcUrl" : "jdbc:hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
                        "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);
    sql:Parameter[] parameters=[];
    datatable df;
    int i;
    long l;
    float f;
    double d;
    boolean b;
    string s;

    df = sql:ClientConnector.select(testDB, "SELECT int_type, long_type, float_type, double_type, boolean_type,
                string_type from DataTable LIMIT 1",parameters);
    while (datatables:next(df)) {
        i = datatables:getInt(df, 1);
        l = datatables:getLong(df, 2);
        f = datatables:getFloat(df, 3);
        d = datatables:getDouble(df, 4);
        b = datatables:getBoolean(df, 5);
        s = datatables:getString(df, 6);
    }
    datatables:close(df);
    return i, l, f, d, b, s;
}

function getXXXByName()(int, long, float, double, boolean, string) {
    map propertiesMap = {"jdbcUrl" : "jdbc:hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
                            "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);
    sql:Parameter[] parameters=[];
    datatable df;
    int i;
    long l;
    float f;
    double d;
    boolean b;
    string s;

    df = sql:ClientConnector.select(testDB, "SELECT int_type, long_type, float_type, double_type, boolean_type,
                string_type from DataTable LIMIT 1",parameters);
    while (datatables:next(df)) {
        i = datatables:getInt(df, "int_type");
        l = datatables:getLong(df, "long_type");
        f = datatables:getFloat(df, "float_type");
        d = datatables:getDouble(df, "double_type");
        b = datatables:getBoolean(df, "boolean_type");
        s = datatables:getString(df, "string_type");
    }
    datatables:close(df);
    return i, l, f, d, b, s;
}

function toJson()(json) {
    map propertiesMap = {"jdbcUrl" : "jdbc:hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
                            "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);
    sql:Parameter[] parameters=[];
    datatable df;
    json result;

    df = sql:ClientConnector.select(testDB, "SELECT int_type, long_type, float_type, double_type, boolean_type,
                string_type from DataTable LIMIT 1",parameters);
    result = datatables:toJson(df);
    return result;
}

function toXmlWithWrapper()(xml) {
    map propertiesMap = {"jdbcUrl" : "jdbc:hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
                            "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);
    sql:Parameter[] parameters=[];
    datatable df;
    xml result;

    df = sql:ClientConnector.select(testDB, "SELECT int_type, long_type, float_type, double_type, boolean_type,
                string_type from DataTable LIMIT 1",parameters);
    result = datatables:toXml(df, "types", "type");
    return result;
}

function toXmlComplex() (xml) {
    map propertiesMap = {"jdbcUrl":"jdbc:hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
                         "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);
    sql:Parameter[] parameters=[];
    datatable df;
    xml result;

    df = sql:ClientConnector.select(testDB, "SELECT int_type, int_array, long_type, long_array, float_type,
                float_array, double_type, boolean_type, string_type, double_array, boolean_array, string_array
                from MixTypes LIMIT 1",parameters);
    result = datatables:toXml(df, "types", "type");
    return result;
}

function getByName()(string, string, long, long, long) {
    map propertiesMap = {"jdbcUrl" : "jdbc:hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
                            "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);
    sql:Parameter[] parameters=[];
    datatable df;
    string blob;
    string clob;
    long time;
    long date;
    long timestamp;

    df = sql:ClientConnector.select(testDB, "SELECT blob_type, clob_type, time_type, date_type, timestamp_type
                from ComplexTypes LIMIT 1",parameters);
    while (datatables:next(df)) {
        blob = datatables:getString(df, "blob_type", "blob");
        clob = datatables:getString(df, "clob_type", "clob");
        time = datatables:getLong(df, "time_type", "time");
        date = datatables:getLong(df, "date_type", "date");
        timestamp = datatables:getLong(df, "timestamp_type", "timestamp");
    }
    datatables:close(df);
    return blob, clob, time, date, timestamp;
}

function getByIndex()(string, string, long, long, long) {
    map propertiesMap = {"jdbcUrl" : "jdbc:hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
                            "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);
    sql:Parameter[] parameters=[];
    datatable df;
    string blob;
    string clob;
    long time;
    long date;
    long timestamp;

    sql:ClientConnector.update(testDB, "Update ComplexTypes set clob_type = 'Test String' where row_id = 1",parameters);

    df = sql:ClientConnector.select(testDB, "SELECT blob_type, clob_type, time_type, date_type, timestamp_type
                from ComplexTypes LIMIT 1",parameters);
    while (datatables:next(df)) {
        blob = datatables:getString(df, 1, "blob");
        clob = datatables:getString(df, 2, "clob");
        time = datatables:getLong(df, 3, "time");
        date = datatables:getLong(df, 4, "date");
        timestamp = datatables:getLong(df, 5, "timestamp");
    }
    datatables:close(df);
    return blob, clob, time, date, timestamp;
}

function getObjectAsStringByIndex()(string, string, string, string, string) {
    map propertiesMap = {"jdbcUrl" : "jdbc:hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
                            "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);
    sql:Parameter[] parameters=[];
    datatable df;
    string blob;
    string clob;
    string time;
    string date;
    string timestamp;

    df = sql:ClientConnector.select(testDB, "SELECT blob_type, clob_type, time_type, date_type, timestamp_type
                from ComplexTypes LIMIT 1",parameters);
    while (datatables:next(df)) {
        blob = datatables:getValueAsString(df, 1);
        clob = datatables:getValueAsString(df, 2);
        time = datatables:getValueAsString(df, 3);
        date = datatables:getValueAsString(df, 4);
        timestamp = datatables:getValueAsString(df, 5);
    }
    datatables:close(df);
    return blob, clob, time, date, timestamp;
}

function getObjectAsStringByName()(string, string, string, string, string) {
    map propertiesMap = {"jdbcUrl" : "jdbc:hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
                            "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);
    sql:Parameter[] parameters=[];
    datatable df;
    string blob;
    string clob;
    string time;
    string date;
    string timestamp;

    df = sql:ClientConnector.select(testDB, "SELECT blob_type, clob_type, time_type, date_type, timestamp_type
                from ComplexTypes LIMIT 1",parameters);
    while (datatables:next(df)) {
        blob = datatables:getValueAsString(df, "blob_type");
        clob = datatables:getValueAsString(df, "clob_type");
        time = datatables:getValueAsString(df, "time_type");
        date = datatables:getValueAsString(df, "date_type");
        timestamp = datatables:getValueAsString(df, "timestamp_type");
    }
    datatables:close(df);
    return blob, clob, time, date, timestamp;
}


function getArrayByName()(map int_arr, map long_arr, map double_arr, map string_arr, map boolean_arr) {
    map propertiesMap = {"jdbcUrl" : "jdbc:hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
                            "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);
    sql:Parameter[] parameters=[];
    datatable df;

    df = sql:ClientConnector.select(testDB, "SELECT int_array, long_array, double_array, boolean_array, string_array
                from ArrayTypes LIMIT 1",parameters);
    while (datatables:next(df)) {
        int_arr = datatables:getArray(df, "int_array");
        long_arr = datatables:getArray(df, "long_array");
        double_arr = datatables:getArray(df, "double_array");
        boolean_arr = datatables:getArray(df, "boolean_array");
        string_arr = datatables:getArray(df, "string_array");
    }
    datatables:close(df);
    return;
}

function getArrayByIndex()(map int_arr, map long_arr, map double_arr, map string_arr, map boolean_arr) {
    map propertiesMap = {"jdbcUrl" : "jdbc:hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB",
                            "username":"SA", "password":"", "maximumPoolSize":1};
    sql:ClientConnector testDB = create sql:ClientConnector(propertiesMap);
    sql:Parameter[] parameters=[];
    datatable df;

    df = sql:ClientConnector.select(testDB, "SELECT int_array, long_array, double_array, boolean_array, string_array
                from ArrayTypes LIMIT 1",parameters);
    while (datatables:next(df)) {
        int_arr = datatables:getArray(df, 1);
        long_arr = datatables:getArray(df, 2);
        double_arr = datatables:getArray(df, 3);
        boolean_arr = datatables:getArray(df, 4);
        string_arr = datatables:getArray(df, 5);
    }
    datatables:close(df);
    return;
}