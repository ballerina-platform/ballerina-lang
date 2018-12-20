import ballerina/test;
import ballerina/io;
import ballerina/http;

@test:Config { }
function testFunc() {
    // Invoking the main function
    http:Client httpEndpoint = new ("http://localhost:9090");

    string response1 = "Order Processed!";

    // Send a GET request to the specified endpoint
    var response = httpEndpoint->get("/online-store-service/make-order");
    if response is http:Response {
        var res = response.getTextPayload();
        if res is error {
            test:assertFail(msg = "Failed to call the endpoint:");
        } else {
            test:assertEquals(res, response1);
            }
    } else {
        test:assertFail(msg = "Failed to call the endpoint:");
    }
}

function stopService() {
    test:stopServices("gauge-metrics");
}