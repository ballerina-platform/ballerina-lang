module io.ballerina.LSExtensions.PartialParserService {
    requires io.ballerina.diagram.util;
    requires io.ballerina.formatter.core;
    requires io.ballerina.lang;
    requires io.ballerina.parser;
    requires io.ballerina.tools.api;
    requires org.eclipse.lsp4j.jsonrpc;
    requires org.eclipse.lsp4j;
    requires io.ballerina.language.server.commons;
    requires io.ballerina.language.server.core;
    requires com.google.gson;

    exports io.ballerina.parsers;
}
