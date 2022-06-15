module io.ballerina.customrecords.customRecordGenerator  {
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires io.ballerina.formatter.core;
    requires io.ballerina.lang;
    requires io.ballerina.parser;
    requires io.ballerina.tools.api;
    requires org.eclipse.lsp4j.jsonrpc;
    requires io.ballerina.language.server.commons;
    requires org.eclipse.lsp4j;

    exports io.ballerina.generators;
    exports io.ballerina.generators.utils;
    exports io.ballerina.generators.exceptions;
    exports io.ballerina.generators.pojo;
    exports io.ballerina.generators.platform;
}