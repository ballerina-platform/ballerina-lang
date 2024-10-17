module io.ballerina.xmltorecordconverter {
    requires io.ballerina.formatter.core;
    requires io.ballerina.identifier;
    requires io.ballerina.lang;
    requires io.ballerina.language.server.commons;
    requires io.ballerina.parser;
    requires io.ballerina.tools.api;
    requires java.xml;
    requires org.apache.commons.lang3;
    requires org.eclipse.lsp4j.jsonrpc;

    exports io.ballerina.xmltorecordconverter;
}
