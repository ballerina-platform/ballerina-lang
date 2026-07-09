module org.ballerinalang.oci {
    requires com.google.cloud.tools.jib;
    requires com.google.cloud.tools.jib.api.buildplan;
    requires io.ballerina.central.client;
    requires okhttp3;
    requires com.google.gson;
    requires progressbar;
    requires java.logging;
    requires com.google.api.client;
    exports org.ballerinalang.oci;
}
