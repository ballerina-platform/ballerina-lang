import ballerina.net.http;

service<http> Service1 {

    @http:GET { }
    resource Resource1 (message m) {
        reply m;
    }
}
