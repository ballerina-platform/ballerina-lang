import ballerina/http;

service<http:Service> hello bind { port:9090 } {
    int /*def*/a;
    sayHello(endpoint caller, http:Request req) {
        /*ref*/a =10;
    }
}
