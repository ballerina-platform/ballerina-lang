import ballerina.lang.datatable;
import ballerina.data.sql;

function getXXXByIndex()(int, long, float, double, boolean, string) {
    sql:Connector testDB = new sql:Connector({"jdbcUrl" : "jdbc:h2:file:./target/TEST_DATA_TABLE_DB2", "driverClassName":"org.h2.Driver", "username":"root", "password":"root", "maximumPoolSize": 1});
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
    sql:Connector testDB = new sql:Connector({"jdbcUrl" : "jdbc:h2:file:./target/TEST_DATA_TABLE_DB2", "driverClassName":"org.h2.Driver", "username":"root", "password":"root", "maximumPoolSize": 1});
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
    sql:Connector testDB = new sql:Connector({"jdbcUrl" : "jdbc:h2:file:./target/TEST_DATA_TABLE_DB2", "driverClassName":"org.h2.Driver", "username":"root", "password":"root", "maximumPoolSize": 1});
    datatable df;
    json result;

    df = sql:Connector.select(testDB, "SELECT int_type, long_type, float_type, double_type, boolean_type, string_type from DataTable LIMIT 1");
    result = datatable:toJson(df);
    return result;
}

function toXmlWithWrapper()(xml) {
    sql:Connector testDB = new sql:Connector({"jdbcUrl" : "jdbc:h2:file:./target/TEST_DATA_TABLE_DB2", "driverClassName":"org.h2.Driver", "username":"root", "password":"root", "maximumPoolSize": 1});
    datatable df;
    xml result;

    df = sql:Connector.select(testDB, "SELECT int_type, long_type, float_type, double_type, boolean_type, string_type from DataTable LIMIT 1");
    result = datatable:toXml(df, "types", "type");
    return result;
}

function getByName()(string, string, long, long, long) {
    sql:Connector testDB = new sql:Connector({"jdbcUrl" : "jdbc:h2:file:./target/TEST_DATA_TABLE_DB2", "driverClassName":"org.h2.Driver", "username":"root", "password":"root", "maximumPoolSize": 1});
    datatable df;
    string blob;
    string clob;
    long time;
    long date;
    long timestamp;

    df = sql:Connector.select(testDB, "SELECT blob_type, clob_type, time_type, date_type, timestamp_type from ComplexTypes LIMIT 1");
    while (datatable:next(df)) {
        blob = datatable:getString(df, "blob_type", "blob");
//        clob = datatable:getString(df, "clob_type", "clob");
        time = datatable:getLong(df, "time_type", "time");
        date = datatable:getLong(df, "date_type", "date");
        timestamp = datatable:getLong(df, "timestamp_type", "timestamp");
    }
    datatable:close(df);
    return blob, clob, time, date, timestamp;
}

function getByIndex()(string, string, long, long, long) {
    sql:Connector testDB = new sql:Connector({"jdbcUrl" : "jdbc:h2:file:./target/TEST_DATA_TABLE_DB2", "driverClassName":"org.h2.Driver", "username":"root", "password":"root", "maximumPoolSize": 1});
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
//        clob = datatable:getString(df, 2, "clob");
        time = datatable:getLong(df, 3, "time");
        date = datatable:getLong(df, 4, "date");
        timestamp = datatable:getLong(df, 5, "timestamp");
    }
    datatable:close(df);
    return blob, clob, time, date, timestamp;
}

function getObjectAsStringByIndex()(string, string, string, string) {
    sql:Connector testDB = new sql:Connector({"jdbcUrl" : "jdbc:h2:file:./target/TEST_DATA_TABLE_DB2", "driverClassName":"org.h2.Driver", "username":"root", "password":"root", "maximumPoolSize": 1});
    datatable df;
    string blob;
    string time;
    string date;
    string timestamp;

    df = sql:Connector.select(testDB, "SELECT blob_type, time_type, date_type, timestamp_type from ComplexTypes LIMIT 1");
    while (datatable:next(df)) {
        blob = datatable:getObjectAsString(df, 1);
        time = datatable:getObjectAsString(df, 2);
        date = datatable:getObjectAsString(df, 3);
        timestamp = datatable:getObjectAsString(df, 4);
    }
    datatable:close(df);
    return blob, time, date, timestamp;
}

function getObjectAsStringByName()(string, string, string, string) {
    sql:Connector testDB = new sql:Connector({"jdbcUrl" : "jdbc:h2:file:./target/TEST_DATA_TABLE_DB2", "driverClassName":"org.h2.Driver", "username":"root", "password":"root", "maximumPoolSize": 1});
    datatable df;
    string blob;
    string time;
    string date;
    string timestamp;

    df = sql:Connector.select(testDB, "SELECT blob_type, time_type, date_type, timestamp_type from ComplexTypes LIMIT 1");
    while (datatable:next(df)) {
        blob = datatable:getObjectAsString(df, "blob_type");
        time = datatable:getObjectAsString(df, "time_type");
        date = datatable:getObjectAsString(df, "date_type");
        timestamp = datatable:getObjectAsString(df, "timestamp_type");
    }
    datatable:close(df);
    return blob, time, date, timestamp;
}


function getArray()(int[]) {
    sql:Connector testDB = new sql:Connector({"jdbcUrl" : "jdbc:h2:file:./target/TEST_DATA_TABLE_DB2", "driverClassName":"org.h2.Driver", "username":"root", "password":"root", "maximumPoolSize": 1});
    datatable df;
    int[] int_arr;

    df = sql:Connector.select(testDB, "SELECT blob_type from ArrayTypes LIMIT 1");
    while (datatable:next(df)) {
        int_arr = datatable:getIntArray(df, "blob_type");
    }
    datatable:close(df);
    return int_arr;
}

