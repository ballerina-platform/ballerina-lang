import ballerina/http;
import ballerina/test;
import ballerina/log;
import ballerina/io;

service helloTest on new http:Listener(9092) {
    resource function sayHello(http:Caller caller, http:Request req) {
        var result = caller->respond("Hello, Test!");
        if (result is error) {
            log:printError("Error sending response", result);
        }
    }
}

# Test service
@test:Config {

}
function testFunction() {
    http:Client clientModule = new ("http://localhost:9090");
    http:Client clientTest = new ("http://localhost:9092");
    var respModule = clientModule->get("/helloModule/sayHello");
    var respTest = clientTest->get("/helloTest/sayHello");

    if (respModule is http:Response) {
        var payload = respModule.getTextPayload();
        if (payload is string) {
            io:println(payload);
        } else {
            io:println(payload.detail());
        }
    } else {
        io:println(respModule.detail());
    }

    if (respTest is http:Response) {
        var payload = respTest.getTextPayload();
        if (payload is string) {
            io:println(payload);
        } else {
            io:println(payload.detail());
        }
    } else {
        io:println(respTest.detail());
    }
}

