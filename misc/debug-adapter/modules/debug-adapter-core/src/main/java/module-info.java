module ballerina.debug.adapter.core {
    exports org.ballerinalang.debugadapter.launcher;
    requires org.eclipse.lsp4j.debug;
    requires jdk.jdi;
    requires slf4j.api;
    requires org.apache.commons.compress;
    requires ballerina.lang;
    requires org.eclipse.lsp4j.jsonrpc;
}