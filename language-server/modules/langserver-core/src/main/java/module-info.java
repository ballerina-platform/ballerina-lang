module io.ballerina.language.server.core {
    exports org.ballerinalang.langserver;
    exports org.ballerinalang.langserver.client;
    requires org.eclipse.lsp4j;
    requires io.ballerina.language.server.compiler;
    requires io.ballerina.language.server.commons;
    requires gson;
    requires org.apache.commons.lang3;
    requires org.eclipse.lsp4j.jsonrpc;
    requires io.ballerina.lang;
    requires io.ballerina.jvm;
    requires slf4j.api;
    requires org.apache.commons.io;
    requires io.ballerina.openapi.convertor;
    requires handlebars;
    requires io.ballerina.parser;
    requires jsr305;
    requires antlr4.runtime;
    requires toml4j;
}
