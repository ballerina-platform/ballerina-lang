import ballerina/test;
import ballerina/io;
import ballerina/http;

@test:Config
function testFunc() {
    endpoint http:Client httpEndpoint { url: "http://localhost:9090" };
    endpoint http:Client httpPassthroughEndpoint { url: "http://localhost:9092" };

    json expectedJson = {"Type":"Always but constrained by content-type"};
    var response = httpEndpoint->get("/alwaysCompress/getJson");
    match response {
        http:Response resp => {
            json res = check resp.getJsonPayload();
            test:assertEquals(res, expectedJson);
        }
        error err => test:assertFail(msg = "Failed to call the endpoint:");
    }

    string expectedString = "Backend response was encoded : deflate, gzip";
    response = httpPassthroughEndpoint->get("/passthrough/");
    match response {
        http:Response resp => {
            string res = check resp.getTextPayload();
            test:assertEquals(res, expectedString);
        }
        error err => test:assertFail(msg = "Failed to call the endpoint:");
    }
}
