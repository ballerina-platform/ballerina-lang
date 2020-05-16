module io.ballerina.mysql {
    requires java.sql;
    requires transitive io.ballerina.jvm;
    requires io.ballerina.sql;
    requires io.ballerina.crypto;
    exports org.ballerinalang.mysql;
}