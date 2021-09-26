module io.ballerina.central.client {
    exports org.ballerinalang.central.client;
    exports org.ballerinalang.central.client.model;
    exports org.ballerinalang.central.client.exceptions;
    exports org.ballerinalang.central.client.model.connector;
    requires gson;
    requires jsr305;
    requires java.net.http;
    requires progressbar;
    requires jdk.httpserver;
    requires io.ballerina.runtime;
    requires java.semver;
    requires org.apache.commons.io;
    requires okhttp3;
    requires okio;
}
