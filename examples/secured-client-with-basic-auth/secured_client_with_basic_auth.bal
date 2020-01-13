import ballerina/auth;
import ballerina/http;
import ballerina/log;

// Defines the Basic Auth client endpoint to call the backend services.
// Basic Authentication is enabled by creating an
// `auth:OutboundBasicAuthProvider` with the `username` and `password`
// passed as a record.
auth:OutboundBasicAuthProvider outboundBasicAuthProvider = new({
    username: "tom",
    password: "1234"
});

// Creates a Basic Auth handler with the created Basic Auth provider.
http:BasicAuthHandler outboundBasicAuthHandler =
                                            new(outboundBasicAuthProvider);

http:Client httpEndpoint = new("https://localhost:9090", {
    auth: {
        authHandler: outboundBasicAuthHandler
    },
    secureSocket: {
        trustStore: {
            path: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
            password: "ballerina"
        }
    }
});

public function main() {
    // Send a `GET` request to the specified endpoint.
    var response = httpEndpoint->get("/hello/sayHello");
    if (response is http:Response) {
        var result = response.getTextPayload();
        log:printInfo((result is error) ? "Failed to retrieve payload."
                                        : result);
    } else {
        log:printError("Failed to call the endpoint.", response);
    }
}
