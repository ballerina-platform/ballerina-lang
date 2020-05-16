module ballerina.logging {
    requires java.logging;
    requires ballerina.config;
    requires gson;
    exports org.ballerinalang.logging;
    exports org.ballerinalang.logging.util;
}