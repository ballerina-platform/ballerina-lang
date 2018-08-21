import ballerina/http;

type /*def*/testRecord record {
    string a;
}

service<http:Service> hello bind { port: 9090 } {
    sayHello(endpoint caller, http:Request req) {
        /*ref*/testRecord ts;
    }
}
