module io.ballerina.formatter.core {
    requires io.ballerina.lang;
    requires io.ballerina.parser;
    requires io.ballerina.toml;
    requires io.ballerina.tools.api;
    requires org.apache.commons.lang3;
    requires org.slf4j;

    exports org.ballerinalang.formatter.core;
    exports org.ballerinalang.formatter.core.options;
}
