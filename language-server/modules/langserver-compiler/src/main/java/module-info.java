module io.ballerina.language.server.compiler {
    exports org.ballerinalang.langserver.compiler.workspace;
    exports org.ballerinalang.langserver.compiler;
    exports org.ballerinalang.langserver.compiler.common;
    exports org.ballerinalang.langserver.compiler.config;
    exports org.ballerinalang.langserver.compiler.exception;
    exports org.ballerinalang.langserver.compiler.format;
    exports org.ballerinalang.langserver.compiler.sourcegen;
    exports org.ballerinalang.langserver.compiler.common.modal;
    requires io.ballerina.lang;
    requires io.ballerina.language.server.commons;
    requires org.eclipse.lsp4j;
    requires org.eclipse.lsp4j.jsonrpc;
    requires slf4j.api;
    requires guava;
    requires jsr305;
    requires gson;
    requires org.apache.commons.lang3;
    requires io.ballerina.parser;
    requires io.ballerina.tools.api;
}
