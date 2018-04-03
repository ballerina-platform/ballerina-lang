import ballerina/http;
import ballerina/io;
import ballerina/test;
import ballerina/config;

string url = "http://0.0.0.0:9090/v1";
boolean isServiceSkeletonStarted;

function init() {
    isServiceSkeletonStarted = test:startServiceSkeleton("src/test/resources/service.skeleton/tmp", "mypackage",
                                                         "src/test/resources/service.skeleton/petstore.yaml");
}

function clean() {
    test:stopServiceSkeleton("mypackage");
}

@test:Config{before: "init", after: "clean"}
function testService () {
    endpoint http:ClientEndpoint httpEndpoint {
        targets:[{
            uri:url
        }]
    };

    test:assertTrue(isServiceSkeletonStarted, msg = "Service skeleton failed to start");

    http:Request req = {};
    // Send a GET request to the specified endpoint
    var response = httpEndpoint -> get("/pets", req);
    match response {
               http:Response resp => {
                    var strRes = resp.getStringPayload();
                    string expected = "Sample listPets Response";
                    test:assertEquals(strRes, expected);
               }
               http:HttpConnectorError err => test:assertFail(msg = "Failed to call the endpoint: "+url);
    }
}