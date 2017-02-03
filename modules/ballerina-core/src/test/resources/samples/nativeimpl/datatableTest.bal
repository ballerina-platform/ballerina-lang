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