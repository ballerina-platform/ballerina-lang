module io.ballerina.LSExtensions.BalShellService {
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
