module org.ballerinalang.observability.anaylze {
    requires io.ballerina.lang;
    requires io.ballerina.parser;
    requires io.ballerina.tools.api;
    requires io.ballerina.language.server.compiler;
    requires io.ballerina.language.server.core;
    requires io.ballerina.diagram.util;

    requires gson;

    provides org.wso2.ballerinalang.compiler.spi.ObservabilitySymbolCollector
            with org.ballerinalang.observability.anaylze.DefaultObservabilitySymbolCollector;
}
