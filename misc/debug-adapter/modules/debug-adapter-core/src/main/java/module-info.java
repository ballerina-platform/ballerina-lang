module ballerina.debug.adapter.core {
    uses org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider;
    requires org.eclipse.lsp4j.debug;
    requires jdk.jdi;
    requires slf4j.api;
    requires io.ballerina.lang;
    requires io.ballerina.tools.api;
    requires io.ballerina.parser;
    requires io.ballerina.runtime;
    requires org.eclipse.lsp4j.jsonrpc;
    requires io.ballerina.language.server.commons;
    requires jsr305;

    exports org.ballerinalang.debugadapter.launcher;
}
