import ballerina/http;
import ballerina/log;

// Create a client configuration to be passed to the client endpoint.
// Configure the `keyStoreFile`, `keyStorePassword`, `trustStoreFile`, and
// the`trustStorePassword`, which are required to enable mutual SSL.
http:ClientConfiguration clientEPConfig = {
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
            log:printError(<string>payload.detail()["message"]);
        }
    } else {
        // If an error occurs while getting the response, log the error.
        log:printError(<string>resp.detail()["message"]);
    }
}
