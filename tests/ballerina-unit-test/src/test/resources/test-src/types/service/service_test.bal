import ballerina/http;

function testServiceType () returns (service) {
    service ts = HelloWorld;
    return ts;
}

service HelloWorld on new http:MockListener (9090) {
    resource function hello (http:Caller caller, http:Request req) {
        http:Response res = new;
        res.setTextPayload("Hello, World!");
        checkpanic caller->respond(res);
    }
}