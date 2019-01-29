import ballerina/test;
import ballerina/io;
import ballerina/http;

boolean serviceStarted = false;

function startService() {
    //serviceStarted = test:startServices("config-api");
}

// Execute this test as ballerina test config-api -e hello.keystore.password=@encrypted:{jFMAXsuMSiOCaxuDLuQjVXzMzZxQrten0652/j93Amw=}
// then enter 12345 as the secret
@test:Config {
    before: "startService",
    after: "stopService"
}
function testFunc() {
    // Invoking the main function
    http:Client httpEndpoint = new("https://localhost:8085", config = {
        secureSocket:{
            trustStore:{
                path: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
                password: "ballerina"
            }
        }
    });
    // Chck whether the server is started
    //test:assertTrue(serviceStarted, msg = "Unable to start the service");

    string expectedValue = "Hello World!";

    // Send a GET request to the specified endpoint
    var response = httpEndpoint->get("/hello");
    if (response is http:Response) {
        var res = response.getTextPayload();
        test:assertEquals(res, expectedValue);
    } else {
        test:assertFail(msg = "Failed to call the endpoint:");
    }
}

function stopService() {
    //test:stopServices("config-api");
}