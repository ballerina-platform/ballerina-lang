import ballerina.lang.datatable;
import ballerina.data.sql;

function getXXXByIndex()(int, long, float, double, boolean, string) {
    sql:Connector testDB = new sql:Connector({"jdbcUrl" : "jdbc:hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB2", "username":"SA", "password":"", "maximumPoolSize": 1});
    datatable df;
    int i;
    long l;
    float f;
    double d;
    boolean b;
    string s;

    df = sql:Connector.select(testDB, "SELECT int_type, long_type, float_type, double_type, boolean_type, string_type from DataTable LIMIT 1");
    while (datatable:next(df)) {
        i = datatable:getInt(df, 1);
        l = datatable:getLong(df, 2);
        f = datatable:getFloat(df, 3);
        d = datatable:getDouble(df, 4);
        b = datatable:getBoolean(df, 5);
        s = datatable:getString(df, 6);
    }
    datatable:close(df);
    return i, l, f, d, b, s;
}

function getXXXByName()(int, long, float, double, boolean, string) {
    sql:Connector testDB = new sql:Connector({"jdbcUrl" : "jdbc:hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB2", "username":"SA", "password":"", "maximumPoolSize": 1});
    datatable df;
    int i;
    long l;
    float f;
    double d;
    boolean b;
    string s;

    df = sql:Connector.select(testDB, "SELECT int_type, long_type, float_type, double_type, boolean_type, string_type from DataTable LIMIT 1");
    while (datatable:next(df)) {
        i = datatable:getInt(df, "int_type");
        l = datatable:getLong(df, "long_type");
        f = datatable:getFloat(df, "float_type");
        d = datatable:getDouble(df, "double_type");
        b = datatable:getBoolean(df, "boolean_type");
        s = datatable:getString(df, "string_type");
    }
    datatable:close(df);
    return i, l, f, d, b, s;
}

function toJson()(json) {
    sql:Connector testDB = new sql:Connector({"jdbcUrl" : "jdbc:hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB2", "username":"SA", "password":"", "maximumPoolSize": 1});
    datatable df;
    json result;

    df = sql:Connector.select(testDB, "SELECT int_type, long_type, float_type, double_type, boolean_type, string_type from DataTable LIMIT 1");
    result = datatable:toJson(df);
    return result;
}

function toXmlWithWrapper()(xml) {
    sql:Connector testDB = new sql:Connector({"jdbcUrl" : "jdbc:hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB2", "username":"SA", "password":"", "maximumPoolSize": 1});
    datatable df;
    xml result;

    df = sql:Connector.select(testDB, "SELECT int_type, long_type, float_type, double_type, boolean_type, string_type from DataTable LIMIT 1");
    result = datatable:toXml(df, "types", "type");
    return result;
}

function getByName()(string, string, long, long, long) {
    sql:Connector testDB = new sql:Connector({"jdbcUrl" : "jdbc:hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB2", "username":"SA", "password":"", "maximumPoolSize": 1});
    datatable df;
    string blob;
    string clob;
    long time;
    long date;
    long timestamp;

    df = sql:Connector.select(testDB, "SELECT blob_type, clob_type, time_type, date_type, timestamp_type from ComplexTypes LIMIT 1");
    while (datatable:next(df)) {
        blob = datatable:getString(df, "blob_type", "blob");
        clob = datatable:getString(df, "clob_type", "clob");
        time = datatable:getLong(df, "time_type", "time");
        date = datatable:getLong(df, "date_type", "date");
        timestamp = datatable:getLong(df, "timestamp_type", "timestamp");
    }
    datatable:close(df);
    return blob, clob, time, date, timestamp;
}

function getByIndex()(string, string, long, long, long) {
    sql:Connector testDB = new sql:Connector({"jdbcUrl" : "jdbc:hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB2", "username":"SA", "password":"", "maximumPoolSize": 1});
    datatable df;
    string blob;
    string clob;
    long time;
    long date;
    long timestamp;

    sql:Connector.update(testDB, "Update ComplexTypes set clob_type = 'Test String' where row_id = 1");

    df = sql:Connector.select(testDB, "SELECT blob_type, clob_type, time_type, date_type, timestamp_type from ComplexTypes LIMIT 1");
    while (datatable:next(df)) {
        blob = datatable:getString(df, 1, "blob");
        clob = datatable:getString(df, 2, "clob");
        time = datatable:getLong(df, 3, "time");
        date = datatable:getLong(df, 4, "date");
        timestamp = datatable:getLong(df, 5, "timestamp");
    }
    datatable:close(df);
    return blob, clob, time, date, timestamp;
}

function getObjectAsStringByIndex()(string, string, string, string, string) {
    sql:Connector testDB = new sql:Connector({"jdbcUrl" : "jdbc:hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB2", "username":"SA", "password":"", "maximumPoolSize": 1});
    datatable df;
    string blob;
    string clob;
    string time;
    string date;
    string timestamp;

    df = sql:Connector.select(testDB, "SELECT blob_type, clob_type, time_type, date_type, timestamp_type from ComplexTypes LIMIT 1");
    while (datatable:next(df)) {
        blob = datatable:getObjectAsString(df, 1);
        clob = datatable:getObjectAsString(df, 2);
        time = datatable:getObjectAsString(df, 3);
        date = datatable:getObjectAsString(df, 4);
        timestamp = datatable:getObjectAsString(df, 5);
    }
    datatable:close(df);
    return blob, clob, time, date, timestamp;
}

function getObjectAsStringByName()(string, string, string, string, string) {
    sql:Connector testDB = new sql:Connector({"jdbcUrl" : "jdbc:hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB2", "username":"SA", "password":"", "maximumPoolSize": 1});
    datatable df;
    string blob;
    string clob;
    string time;
    string date;
    string timestamp;

    df = sql:Connector.select(testDB, "SELECT blob_type, clob_type, time_type, date_type, timestamp_type from ComplexTypes LIMIT 1");
    while (datatable:next(df)) {
        blob = datatable:getObjectAsString(df, "blob_type");
        clob = datatable:getObjectAsString(df, "clob_type");
        time = datatable:getObjectAsString(df, "time_type");
        date = datatable:getObjectAsString(df, "date_type");
        timestamp = datatable:getObjectAsString(df, "timestamp_type");
    }
    datatable:close(df);
    return blob, clob, time, date, timestamp;
}


function getArrayByName()(int[], long[], double[], string[], boolean[]) {
    sql:Connector testDB = new sql:Connector({"jdbcUrl" : "jdbc:hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB2", "username":"SA", "password":"", "maximumPoolSize": 1});
    datatable df;
    int[] int_arr;
    long[] long_arr;
    double[] double_arr;
    string[] string_arr;
    boolean[] boolean_arr;

    df = sql:Connector.select(testDB, "SELECT int_array, long_array, double_array, boolean_array, string_array from ArrayTypes LIMIT 1");
    while (datatable:next(df)) {
        int_arr = datatable:getIntArray(df, "int_array");
        long_arr = datatable:getLongArray(df, "long_array");
        double_arr = datatable:getDoubleArray(df, "double_array");
        boolean_arr = datatable:getBooleanArray(df, "boolean_array");
        string_arr = datatable:getStringArray(df, "string_array");
    }
    datatable:close(df);
    return int_arr, long_arr, double_arr, string_arr, boolean_arr;
}

function getArrayByIndex()(int[], long[], double[], string[], boolean[]) {
    sql:Connector testDB = new sql:Connector({"jdbcUrl" : "jdbc:hsqldb:file:./target/tempdb/TEST_DATA_TABLE_DB2", "username":"SA", "password":"", "maximumPoolSize": 1});
    datatable df;
    int[] int_arr;
    long[] long_arr;
    double[] double_arr;
    string[] string_arr;
    boolean[] boolean_arr;

    df = sql:Connector.select(testDB, "SELECT int_array, long_array, double_array, boolean_array, string_array from ArrayTypes LIMIT 1");
    while (datatable:next(df)) {
        int_arr = datatable:getIntArray(df, 1);
        long_arr = datatable:getLongArray(df, 2);
        double_arr = datatable:getDoubleArray(df, 3);
        boolean_arr = datatable:getBooleanArray(df, 4);
        string_arr = datatable:getStringArray(df, 5);
    }
    datatable:close(df);
    return int_arr, long_arr, double_arr, string_arr, boolean_arr;
}

