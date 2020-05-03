module ballerina.sql {
    requires ballerina.runtime;
    requires com.zaxxer.hikari;
    requires java.sql;
    requires ballerina.io;
    requires ballerina.time;
    requires ballerina.lang;
    exports org.ballerinalang.sql.datasource;
    exports org.ballerinalang.sql.utils;
}