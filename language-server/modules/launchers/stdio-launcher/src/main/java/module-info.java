module io.ballerina.language.server.stdio.launcher {
    requires io.ballerina.language.server.core;
    requires org.eclipse.lsp4j.jsonrpc;
    requires java.logging;
    exports org.ballerinalang.langserver.launchers.stdio;
}
