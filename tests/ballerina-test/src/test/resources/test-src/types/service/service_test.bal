import ballerina/http;

function testServiceType () returns (typedesc) {
    typedesc ts = typeof HelloWorld;
    return ts;
}

service<http:Service> HelloWorld {
    hello (endpoint outboundEP, http:Request req) {
        http:Response res = {};
        res.setStringPayload("Hello, World!");
        _ = outboundEP -> respond(res);
    }
}