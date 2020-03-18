import ballerina/http;
import ballerina/test;

@test:Config {}
function testFunc() {
    http:Client httpEndpoint = new("https://localhost:9095", config = {
        secureSocket: {
            keyStore: {
                path: "${ballerina.home}/bre/security/ballerinaKeystore.p12",
                password: "ballerina"
            },
            trustStore: {
                path: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
                password: "ballerina"
            },
            protocol: {
                name: "TLS"
            },
            ciphers: ["TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA"]
        }
    });

    string response1 = "Successful";
    var response = httpEndpoint->get("/hello");
    if (response is http:Response) {
        test:assertEquals(response.getTextPayload(), response1);
    } else {
        test:assertFail(msg = "Failed to call the endpoint:");
    }
}
