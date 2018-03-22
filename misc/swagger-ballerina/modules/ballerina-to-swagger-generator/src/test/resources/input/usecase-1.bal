import ballerina/net.http;

service<http> Service1 {

    @http:resourceConfig {
        methods:["GET"]
    }
    resource Resource1 (message m) {
        reply m;
    }
}
