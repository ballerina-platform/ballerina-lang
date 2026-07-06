module io.ballerina.central.client {
    exports org.ballerinalang.central.client;
    exports org.ballerinalang.central.client.model;
    exports org.ballerinalang.central.client.exceptions;
    opens org.ballerinalang.central.client.model to com.google.gson;
    requires com.google.gson;
    requires jsr305;
    requires java.net.http;
    requires progressbar;
    requires jdk.httpserver;
    requires io.ballerina.runtime;
    requires java.semver;
    requires org.apache.commons.io;
    requires kotlin.stdlib;
    requires okhttp3;
    requires okio;
}
