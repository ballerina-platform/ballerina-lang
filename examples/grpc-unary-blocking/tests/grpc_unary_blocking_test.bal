// This is the B7a test for unary blocking scenario.
import ballerina/grpc;
import ballerina/test;

// Client endpoint configuration
HelloWorldBlockingClient blockingEp = new("http://localhost:9090");

@test:Config
function testUnaryBlockingService() {
    // Writes custom headers to request message.
    grpc:Headers headers = new;
    headers.setEntry("client_header_key", "Request Header Value");

    // Executes unary blocking call with headers.
    var unionResp = blockingEp->hello("WSO2", headers);
    if (unionResp is error) {
        string errorMsg = "Error from Connector: " + unionResp.reason() + " - " + <string> unionResp.detail()["message"];
        test:assertFail(msg = errorMsg);
    } else {
        string result;
        grpc:Headers resHeaders;
        [result, resHeaders] = unionResp;
        string expected = "Hello WSO2";
        test:assertEquals(result, expected);
        string headerValue = resHeaders.get("server_header_key") ?: "none";
        string expectedHeaderValue = "Response Header value";
        test:assertEquals(headerValue, expectedHeaderValue);
    }
}
