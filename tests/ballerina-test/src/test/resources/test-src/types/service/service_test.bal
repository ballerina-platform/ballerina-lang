import ballerina.net.http;

function testServiceType () returns (type) {
    type ts = typeof HelloWorld;
    return ts;
}

service<http:Service> HelloWorld {
    resource hello (http:ServerConnector conn, http:Request req) {
        http:Response res = {};
        res.setStringPayload("Hello, World!");
        _ = conn -> respond(res);
    }
}