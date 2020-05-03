import ballerina/config;
import ballerina/http;
import ballerina/log;

// This is a client endpoint configured to connect to the HTTPS service.
// As this is a 1-way SSL connection, the client needs to provide
// trust store file path and its password.
http:ClientConfiguration clientEPConfig = {
    secureSocket: {
        trustStore: {
            path: config:getAsString("b7a.home") +
                  "/bre/security/ballerinaKeystore.p12",
            password: "ballerina"
        }
    }
};

public function main() {
    // Create an HTTP client to interact with the created listener endpoint.
    http:Client clientEP = new("https://localhost:9095", clientEPConfig);
    // Sends an outbound request.
    var resp = clientEP->get("/hello/");
    if (resp is http:Response) {
        // If the request is successful, retrieve the text payload from the
        // response.
        var payload = resp.getTextPayload();
        if (payload is string) {
            // Log the retrieved text paylod.
            log:printInfo(payload);
        } else {
            // If an error occurs when retrieving the text payload, log the error.
            log:printError(<string>payload.detail()["message"]);
        }
    } else {
        // If an error occurs when getting the response, log the error.
        log:printError(<string>resp.detail()["message"]);
    }
}
