import ballerina/http;
import ballerina/test;
import ballerina/io;

# Test service
@test:Config {

}
function testFunction() {
    http:Client clientModule = new ("http://localhost:9091");
    var respModule = clientModule->get("/api/v1/Test/Parametes");

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
}

