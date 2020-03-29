import ballerina/test;
import ballerina/http;

@test:Config {
}
function testFunc() {
    // Invoking the main function
    http:Client httpEndpoint = new("http://localhost:9090");

    // Send a GET request to the specified endpoint
    var response1 = httpEndpoint->get("/lb");
    if (response1 is http:Response) {
        var result = response1.getTextPayload();
        if (result is string) {
            test:assertEquals(result, "Mock1 resource was invoked.");
        } else {
            test:assertFail(msg = "Invalid response message:");
        }
    } else {
        test:assertFail(msg = "Failed to call the endpoint:");
    }

    var response2 = httpEndpoint->get("/lb");
    if (response2 is http:Response) {
        var result = response2.getTextPayload();
        if (result is string) {
            test:assertEquals(result, "Mock2 resource was invoked.");
        } else {
            test:assertFail(msg = "Invalid response message:");
        }
    } else {
        test:assertFail(msg = "Failed to call the endpoint:");
    }

    // Send a GET request to the specified endpoint
    var response3 = httpEndpoint->get("/lb");
    if (response3 is http:Response) {
        var result = response3.getTextPayload();
        if (result is string) {
            test:assertEquals(result, "Mock3 resource was invoked.");
        } else {
            test:assertFail(msg = "Invalid response message:");
        }
    } else {
        test:assertFail(msg = "Failed to call the endpoint:");
    }

    // Send a GET request to the specified endpoint
    var response4 = httpEndpoint->get("/lb");
    if (response4 is http:Response) {
        var result = response4.getTextPayload();
        if (result is string) {
            test:assertEquals(result, "Mock1 resource was invoked.");
        } else {
            test:assertFail(msg = "Invalid response message:");
        }
    } else {
        test:assertFail(msg = "Failed to call the endpoint:");
    }
}
