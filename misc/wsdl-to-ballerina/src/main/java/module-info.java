module io.ballerina.wsdltoballerina {
    requires io.ballerina.formatter.core;
    requires io.ballerina.identifier;
    requires io.ballerina.lang;
    requires io.ballerina.language.server.commons;
    requires io.ballerina.parser;
    requires io.ballerina.tools.api;
    requires java.xml;
    requires wsdl4j;
    requires javatuples;
    requires org.apache.commons.lang3;
    requires org.eclipse.lsp4j.jsonrpc;
    requires XmlSchema;
    requires camel.util;
    requires info.picocli;
    requires io.ballerina.cli;

    exports io.ballerina.wsdltoballerina;
    exports io.ballerina.wsdltoballerina.recordgenerator;
    exports io.ballerina.wsdltoballerina.recordgenerator.ballerinair;
    exports io.ballerina.wsdltoballerina.diagnostic;
    exports io.ballerina.wsdltoballerina.wsdlmodel;
}
