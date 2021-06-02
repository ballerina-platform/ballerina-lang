module io.ballerina.LSExtensions.jsonToRecordConverter {
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires io.ballerina.lang;
    requires io.ballerina.parser;
    requires io.swagger.v3.oas.models;
    requires org.apache.commons.lang3;
    requires org.eclipse.lsp4j.jsonrpc;
    requires swagger.parser.core;
    requires swagger.parser.v3;
    requires io.ballerina.language.server.commons;

    exports io.ballerina.converters;
    exports io.ballerina.converters.util;
    exports io.ballerina.converters.exception;
}