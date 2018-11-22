import ballerina/http;
import ballerina/mime;
import ballerina/test;
import ballerina/config;

string uri = "http://0.0.0.0:9095/v1";
boolean isServiceSkeletonStarted = false;

function init() {
    isServiceSkeletonStarted = test:startServiceSkeleton("mypackage",
        "src/test/resources/service.skeleton/petstore.yaml");
}

function clean() {
    test:stopServiceSkeleton("mypackage");
}

@test:Config{before: "init", after: "clean"}
function testService () {
    endpoint http:Client httpEndpoint {
        url:uri
    };

    test:assertTrue(isServiceSkeletonStarted, msg = "Service skeleton failed to start");

    // Send a GET request to the specified endpoint
    var response = httpEndpoint -> get("/pets");
    if (response is http:Response) {
        var strRes = response.getTextPayload();
        string expected = "Sample listPets Response";
        test:assertEquals(strRes, expected);
    } else {
        test:assertFail(msg = "Failed to call the endpoint: " + uri);
    }
}
