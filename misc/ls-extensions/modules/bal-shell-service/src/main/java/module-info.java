module io.ballerina.LSExtensions.BalShellService {
    uses org.ballerinalang.langserver.commons.service.spi.ExtendedLanguageServerService;
    uses org.ballerinalang.langserver.commons.registration.BallerinaServerCapability;
    uses org.ballerinalang.langserver.commons.registration.BallerinaClientCapability;
    uses org.ballerinalang.langserver.commons.registration.BallerinaServerCapabilitySetter;
    uses org.ballerinalang.langserver.commons.registration.BallerinaClientCapabilitySetter;

    requires io.ballerina.lang;
    requires io.ballerina.runtime;
    requires io.ballerina.language.server.commons;
    requires org.eclipse.lsp4j;
    requires org.eclipse.lsp4j.jsonrpc;
    requires io.ballerina.shell;
    requires io.ballerina.shell.cli;
    requires com.fasterxml.jackson.databind;

    exports io.ballerina.shell.service;
}
