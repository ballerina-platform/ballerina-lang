import ballerina/http;

type testRecord record {
    string /*def*/s;
}

service<http:Service> hello bind { port: 9090 }  {
    sayHello(endpoint caller, http:Request req) {
        testRecord ts = {};
        ts./*ref*/s:"";
    }
}
