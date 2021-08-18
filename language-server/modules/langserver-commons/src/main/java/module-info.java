module io.ballerina.language.server.commons {
    requires io.ballerina.lang;
    requires io.ballerina.parser;
    requires io.ballerina.tools.api;
    requires org.eclipse.lsp4j;
    requires org.eclipse.lsp4j.jsonrpc;
    requires com.google.gson;
    requires io.ballerina.toml;
    exports org.ballerinalang.langserver.commons;
    exports org.ballerinalang.langserver.commons.client;
    exports org.ballerinalang.langserver.commons.service.spi;
    exports org.ballerinalang.langserver.commons.trace;
    exports org.ballerinalang.langserver.commons.workspace;
    exports org.ballerinalang.langserver.commons.capability;
    exports org.ballerinalang.langserver.commons.codeaction;
    exports org.ballerinalang.langserver.commons.command;
    exports org.ballerinalang.langserver.commons.command.spi;
    exports org.ballerinalang.langserver.commons.completion;
    exports org.ballerinalang.langserver.commons.codeaction.spi;
    exports org.ballerinalang.langserver.commons.codelenses;
    exports org.ballerinalang.langserver.commons.codelenses.spi;
    exports org.ballerinalang.langserver.commons.completion.spi;
    exports org.ballerinalang.langserver.commons.toml;
    exports org.ballerinalang.langserver.commons.toml.visitor;
    exports org.ballerinalang.langserver.commons.toml.common;
    exports org.ballerinalang.langserver.commons.toml.common.completion;
}
