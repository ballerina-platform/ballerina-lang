module io.ballerina.sql {
    requires io.ballerina.jvm;
    requires com.zaxxer.hikari;
    requires java.sql;
    requires io.ballerina.io;
    requires io.ballerina.time;
    requires io.ballerina.lang;
    exports org.ballerinalang.sql.datasource;
    exports org.ballerinalang.sql.utils;
}