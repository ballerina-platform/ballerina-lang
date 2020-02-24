import ballerina/http;
import ballerina/test;

@test:Config { }
function testFunc() {
    http:Client httpEndpoint = new("https://localhost:9095", config = {
        secureSocket: {
            trustStore: {
                path: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
                password: "ballerina"
            }
        }
    });

    string response1 = "Hello World!";
    var response = httpEndpoint->get("/hello");
    if (response is http:Response) {
        test:assertEquals(response.getTextPayload(), response1);
    } else {
        test:assertFail(msg = "Failed to call the endpoint:");
    }
}
