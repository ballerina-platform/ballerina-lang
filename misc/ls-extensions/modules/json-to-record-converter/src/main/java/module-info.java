module io.ballerina.LSExtensions.jsonToRecordConverter {
    uses org.ballerinalang.langserver.commons.service.spi.ExtendedLanguageServerService;
    uses org.ballerinalang.langserver.commons.registration.BallerinaServerCapability;
    uses org.ballerinalang.langserver.commons.registration.BallerinaClientCapability;
    uses org.ballerinalang.langserver.commons.registration.BallerinaServerCapabilitySetter;
    uses org.ballerinalang.langserver.commons.registration.BallerinaClientCapabilitySetter;

    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.google.gson;
    requires io.ballerina.formatter.core;
    requires io.ballerina.jsonmapper;
    requires io.ballerina.lang;
    requires io.ballerina.language.server.commons;
    requires io.ballerina.parser;
    requires io.ballerina.tools.api;
    requires io.swagger.v3.oas.models;
    requires org.apache.commons.lang3;
    requires org.eclipse.lsp4j.jsonrpc;
    requires swagger.parser.core;
    requires swagger.parser.v3;
    requires org.eclipse.lsp4j;

    exports io.ballerina.converters;
    exports io.ballerina.converters.util;
    exports io.ballerina.converters.exception;
}
