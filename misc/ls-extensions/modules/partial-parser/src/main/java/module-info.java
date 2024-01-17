module io.ballerina.LSExtensions.PartialParserService {
    uses org.ballerinalang.langserver.commons.service.spi.ExtendedLanguageServerService;
    uses org.ballerinalang.langserver.commons.registration.BallerinaServerCapability;
    uses org.ballerinalang.langserver.commons.registration.BallerinaClientCapability;
    uses org.ballerinalang.langserver.commons.registration.BallerinaServerCapabilitySetter;
    uses org.ballerinalang.langserver.commons.registration.BallerinaClientCapabilitySetter;

    requires io.ballerina.diagram.util;
    requires io.ballerina.formatter.core;
    requires io.ballerina.lang;
    requires io.ballerina.parser;
    requires io.ballerina.tools.api;
    requires org.eclipse.lsp4j.jsonrpc;
    requires org.eclipse.lsp4j;
    requires io.ballerina.language.server.commons;
    requires com.google.gson;

    exports io.ballerina.parsers;
}
