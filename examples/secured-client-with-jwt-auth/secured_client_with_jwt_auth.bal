import ballerina/http;
import ballerina/jwt;
import ballerina/log;

// Defines the JWT auth client endpoint to call the backend services.
// JWT authentication is enabled by creating a `jwt:OutboundJWTAuthProvider`
// with/without passing the JWT issuer configurations as a record.
// If the JWT issuer configurations are not passed, a JWT will be searched
// in `runtime:AuthenticationContext` and it will be used for the outbound
// authentication.
jwt:OutboundJwtAuthProvider outboundJwtAuthProvider = new({
    username: "ballerina",
    issuer: "ballerina",
    audience: ["ballerina", "ballerina.org", "ballerina.io"],
    customClaims: { "scope": "hello" },
    keyStoreConfig: {
        keyAlias: "ballerina",
        keyPassword: "ballerina",
        keyStore: {
            path: "${ballerina.home}/bre/security/ballerinaKeystore.p12",
            password: "ballerina"
        }
    }
});

// Create a Bearer Auth handler with the created JWT Auth provider.
http:BearerAuthHandler outboundJwtAuthHandler = new(outboundJwtAuthProvider);

http:Client httpEndpoint = new("https://localhost:9090", {
    auth: {
        authHandler: outboundJwtAuthHandler
    },
    secureSocket: {
        trustStore: {
            path: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
            password: "ballerina"
        }
    }
});

public function main() {
    // Sends a `GET` request to the specified endpoint.
    var response = httpEndpoint->get("/hello/sayHello");
    if (response is http:Response) {
        var result = response.getTextPayload();
        log:printInfo((result is error) ? "Failed to retrieve payload."
                                        : result);
    } else {
        log:printError("Failed to call the endpoint.", response);
    }
}
