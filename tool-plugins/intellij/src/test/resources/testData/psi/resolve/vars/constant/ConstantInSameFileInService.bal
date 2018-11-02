import ballerina/http;

@final int /*def*/a;

service<http:Service> hello bind { port: 9090 } {
    sayHello(endpoint caller, http:Request req) {
        int value = /*ref*/a;
    }
}