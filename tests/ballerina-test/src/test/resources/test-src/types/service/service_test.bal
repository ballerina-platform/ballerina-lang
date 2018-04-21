import ballerina/http;

function testServiceType () returns (typedesc) {
    typedesc ts = HelloWorld;
    return ts;
}

service<http:Service> HelloWorld {
    hello (endpoint caller, http:Request req) {
        http:Response res = new;
        res.setStringPayload("Hello, World!");
        _ = caller -> respond(res);
    }
}