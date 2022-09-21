module io.ballerina.LSExtensions.BallerinaTriggerService {
    uses org.ballerinalang.langserver.commons.service.spi.ExtendedLanguageServerService;
    uses org.ballerinalang.langserver.commons.registration.BallerinaServerCapability;
    uses org.ballerinalang.langserver.commons.registration.BallerinaClientCapability;
    uses org.ballerinalang.langserver.commons.registration.BallerinaServerCapabilitySetter;
    uses org.ballerinalang.langserver.commons.registration.BallerinaClientCapabilitySetter;
    requires io.ballerina.lang;
    requires com.google.gson;
    requires io.ballerina.central.client;
    requires io.ballerina.language.server.commons;
    requires org.eclipse.lsp4j;
    requires org.eclipse.lsp4j.jsonrpc;
}
