module ballerina.mysql {
    requires java.sql;
    requires transitive ballerina.runtime;
    requires ballerina.sql;
    requires ballerina.crypto;
    exports org.ballerinalang.mysql;
}