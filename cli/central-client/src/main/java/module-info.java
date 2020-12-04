module io.ballerina.central.client {
    exports org.ballerinalang.central.client;
    exports org.ballerinalang.central.client.model;
    exports org.ballerinalang.central.client.exceptions;
    requires gson;
    requires jsr305;
    requires java.net.http;
    requires progressbar;
    requires jdk.httpserver;
    requires io.ballerina.runtime;
}
