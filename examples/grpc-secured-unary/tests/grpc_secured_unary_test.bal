// This is the B7a test for the secured connection (HTTPS) scenario.
import ballerina/io;
import ballerina/test;

// Client endpoint configuration with SSL configurations.
HelloWorldBlockingClient helloWorldBlockingEp = new("https://localhost:9090", {
        secureSocket: {
            trustStore: {
                path: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
                password: "ballerina"
            }
        }
    });

@test:Config
function testSecuredUnaryService() {
    // Executes unary blocking secured call.
    var unionResp = helloWorldBlockingEp->hello("WSO2");
    if (unionResp is error) {
        string errorMsg = "Error from Connector: " + unionResp.reason() + " - " + <string> unionResp.detail()["message"];
        test:assertFail(msg = errorMsg);
    } else {
        string result;
        [result, _] = unionResp;
        string expected = "Hello WSO2";
        test:assertEquals(result, expected);
    }
}
