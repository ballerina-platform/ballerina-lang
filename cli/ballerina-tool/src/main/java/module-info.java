module io.ballerina.tool {
    requires io.ballerina.lang;
    requires info.picocli;
    requires io.ballerina.core;
    requires io.ballerina.config;
    requires io.ballerina.jvm;
    requires slf4j.api;
    exports org.ballerinalang.tool;
    exports org.ballerinalang.tool.util;
}