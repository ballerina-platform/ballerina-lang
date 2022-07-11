module io.ballerina.LSExtensions.PerformanceAnalyzerService {
    uses org.ballerinalang.langserver.commons.service.spi.ExtendedLanguageServerService;
    uses org.ballerinalang.langserver.commons.registration.BallerinaServerCapability;
    uses org.ballerinalang.langserver.commons.registration.BallerinaClientCapability;
    uses org.ballerinalang.langserver.commons.registration.BallerinaServerCapabilitySetter;
    uses org.ballerinalang.langserver.commons.registration.BallerinaClientCapabilitySetter;
    requires io.ballerina.lang;
    requires org.eclipse.lsp4j.jsonrpc;
    requires io.ballerina.language.server.commons;
    requires org.eclipse.lsp4j;
    requires io.ballerina.parser;
    requires io.ballerina.tools.api;
    requires org.slf4j;
    requires com.google.gson;
    requires org.apache.commons.lang3;
    requires java.net.http;
}
