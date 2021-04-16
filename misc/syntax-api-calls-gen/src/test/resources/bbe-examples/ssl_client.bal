import ballerina/config;
import ballerina/http;
import ballerina/log;

// Create a client configuration to be passed to the client endpoint.
// Configure the `keyStore` file `path` and `password`, `truststore`
// file `path` and `password`, which are required to enable mutual SSL.
// [secureSocket](https://ballerina.io/swan-lake/learn/api-docs/ballerina/http/records/ClientSecureSocket.html) record provides the SSL related configurations.
http:ClientConfiguration clientEPConfig = {
    secureSocket: {
        keyStore: {
            path: config:getAsString("b7a.home") +
                  "/bre/security/ballerinaKeystore.p12",
            password: "ballerina"
        },
        trustStore: {
            path: config:getAsString("b7a.home") +
                  "/bre/security/ballerinaTruststore.p12",
            password: "ballerina"
        },
        protocol: {
            name: "TLS"
        },
        ciphers: ["TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA"]
    }
};

public function main() {
    // Create an HTTP client to interact with the created listener endpoint.
    http:Client clientEP = new("https://localhost:9095", clientEPConfig);
    // Send a GET request to the listener.
    var resp = clientEP->get("/hello");
    if (resp is http:Response) {
        // If the request is successful, retrieve the text payload from the
        // response.
        var payload = resp.getTextPayload();
        if (payload is string) {
            // Log the retrieved text payload.
            log:printInfo(payload);
        } else {
            // If an error occurs while retrieving the text payload, log
            // the error.
            log:printError(payload.message());
        }
    } else {
        // If an error occurs while getting the response, log the error.
        log:printError((<error>resp).message());
    }
}
