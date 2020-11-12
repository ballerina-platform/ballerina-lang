module io.ballerina.tool {
    requires info.picocli;
    requires io.ballerina.config;
    requires io.ballerina.core;
    requires io.ballerina.lang;
    requires io.ballerina.runtime;
    requires io.ballerina.tools.api;
    requires slf4j.api;
    requires testng;
    exports org.ballerinalang.tool;
    exports org.ballerinalang.tool.util;
}