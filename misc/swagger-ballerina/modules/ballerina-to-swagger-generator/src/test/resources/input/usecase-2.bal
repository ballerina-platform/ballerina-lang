import ballerina/net.http;

service<http> Service1 {

    resource Resource1 (message m) {
        reply m;
    }
}
