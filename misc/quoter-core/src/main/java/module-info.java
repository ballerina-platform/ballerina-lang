module io.ballerina.quoter {
    requires java.sql;
    requires com.google.gson;
    requires io.ballerina.parser;
    requires io.ballerina.tools.api;

    exports io.ballerina.quoter;
    exports io.ballerina.quoter.config;
}
