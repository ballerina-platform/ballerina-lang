module io.ballerina.syntaxapicallsgen {
    requires java.sql;
    requires com.google.gson;
    requires io.ballerina.parser;
    requires io.ballerina.tools.api;
    requires static org.jetbrains.annotations;

    exports io.ballerina.syntaxapicallsgen;
    exports io.ballerina.syntaxapicallsgen.config;
}
