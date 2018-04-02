// This is server implementation for unary blocking/unblocking scenario
import ballerina/io;
import ballerina/grpc;

// Server endpoint configuration
endpoint grpc:Service ep {
    host:"localhost",
    port:9090
};

@grpc:serviceConfig {generateClientConnector:true}
service<grpc:Endpoint> helloWorld bind ep {
    hello (endpoint client, Request req) {
        io:println(req);
        Response res = {};
        res.name = "WSO2";
        res.kind = Bar.SL;
        grpc:ConnectorError err = client -> send(res);
        _ = client -> complete();
    }
}

struct Response {
    string name;
    Bar kind;
}

enum Bar {
    SL,
    US,
    UK
}

struct Request {
    string name;
    Foo kind;
}


enum Foo {
    WSO2,
    IBM,
    ORACLE
}
