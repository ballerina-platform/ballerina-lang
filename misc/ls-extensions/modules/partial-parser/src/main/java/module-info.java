module io.ballerina.LSExtensions.PartialParserService {
    requires io.ballerina.diagram.util;
    requires io.ballerina.lang;
    requires io.ballerina.parser;
    requires io.ballerina.tools.api;
    requires org.eclipse.lsp4j.jsonrpc;
    requires io.ballerina.language.server.commons;
    requires com.google.gson;

    exports io.ballerina.parsers;
}
