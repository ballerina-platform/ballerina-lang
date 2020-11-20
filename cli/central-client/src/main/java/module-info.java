module io.ballerina.central.client {
    exports org.ballerinalang.central.client;
    exports org.ballerinalang.central.client.model;
    requires gson;
    requires jsr305;
    requires java.net.http;
    requires progressbar;
    requires io.ballerina.lang;
    requires jdk.httpserver;
    requires io.ballerina.runtime;
    requires io.ballerina.tool;
//    requires io.ballerina.projects;
}
