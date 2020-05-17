module io.ballerina.tool {
    exports org.ballerinalang.tool;
    exports org.ballerinalang.tool.util;
    requires info.picocli;
    requires io.ballerina.jvm;
    requires io.ballerina.lang;
    requires io.ballerina.config;
    requires io.ballerina.core;
    requires slf4j.api;
    requires testng;
}