import ballerina.net.http;

function testServiceType () returns (type) {
    type ts = typeof HelloWorld;
    return ts;
}

service<http> HelloWorld {
    resource hello (http:Connection conn, http:Request req) {
        http:Response res = {};
        res.setStringPayload("Hello, World!");
        _ = conn.respond(res);
    }
}