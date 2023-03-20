module io.ballerina.jsonmapper {
    requires com.google.gson;
    requires io.ballerina.formatter.core;
    requires io.ballerina.identifier;
    requires io.ballerina.lang;
    requires io.ballerina.language.server.commons;
    requires io.ballerina.parser;
    requires io.ballerina.tools.api;
    requires javatuples;
    requires org.apache.commons.lang3;

    exports io.ballerina.jsonmapper;
    exports io.ballerina.jsonmapper.diagnostic;
}
