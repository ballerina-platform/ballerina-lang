module ballerina.debug.adapter.core {
    requires org.eclipse.lsp4j.debug;
    requires jdk.jdi;
    requires org.slf4j;
    requires io.ballerina.lang;
    requires io.ballerina.tools.api;
    requires io.ballerina.parser;
    requires io.ballerina.runtime;
    requires org.eclipse.lsp4j.jsonrpc;
    requires io.ballerina.identifier;
    requires org.apache.commons.lang3;

    exports org.ballerinalang.debugadapter.launcher;
}
