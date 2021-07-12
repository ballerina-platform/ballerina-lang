module io.ballerina.logging {
    requires java.logging;
    requires io.ballerina.config;
    requires com.google.gson;
    exports org.ballerinalang.logging;
    exports org.ballerinalang.logging.util;
    exports org.ballerinalang.logging.formatters;
}
