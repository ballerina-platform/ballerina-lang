module io.ballerina.diagram.util {
    requires io.ballerina.lang;
    requires io.ballerina.parser;
    requires io.ballerina.tools.api;

    requires gson;
    requires org.apache.commons.lang3;
    requires io.ballerina.docerina;

    exports org.ballerinalang.diagramutil;
    exports org.ballerinalang.diagramutil.connector.models.connector;
    exports org.ballerinalang.diagramutil.connector.generator;
}
