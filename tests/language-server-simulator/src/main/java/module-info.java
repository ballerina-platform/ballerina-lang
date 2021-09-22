module io.ballerina.language.server.simulator {
    uses org.ballerinalang.langserver.simulator.generators.CodeSnippetGenerator;
    requires org.eclipse.lsp4j;
    requires io.ballerina.language.server.commons;
    requires io.ballerina.language.server.core;
    requires org.eclipse.lsp4j.jsonrpc;
    requires io.ballerina.lang;
    requires io.ballerina.parser;
    requires io.ballerina.tools.api;
    requires com.google.gson;
    requires slf4j.api;
}
