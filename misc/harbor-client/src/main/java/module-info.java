module org.ballerinalang.harbor {
    requires com.google.cloud.tools.jib;
    requires com.google.cloud.tools.jib.api.buildplan;
    requires io.ballerina.central.client;
    requires okhttp3;
    requires com.google.gson;
    requires progressbar;
    requires java.logging;
    exports org.ballerinalang.harbor;
}