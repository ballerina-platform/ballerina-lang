module ballerina.debug.adapter.core {
    requires org.eclipse.lsp4j.debug;
    requires jdk.jdi;
    requires slf4j.api;
    requires io.ballerina.lang;
    requires io.ballerina.tools.api;
    requires io.ballerina.parser;
    requires io.ballerina.runtime;
    requires org.eclipse.lsp4j.jsonrpc;
    requires org.apache.commons.io;

    exports org.ballerinalang.debugadapter;
}
