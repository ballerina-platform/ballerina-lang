import ballerina/http;
import ballerina/jwt;
import ballerina/log;
import ballerina/runtime;

// Defines the JWT auth client endpoint to call the backend services.
// JWT authentication is enabled by creating a `jwt:OutboundJWTAuthProvider`
// with/without passing the JWT issuer configurations as a record. If the JWT
// issuer configurations are passed, a new JWT will be issued and it will be
// used for the outbound authentication.
jwt:OutboundJwtAuthProvider outboundJwtAuthProvider = new;

// Create a Bearer Auth handler with the created JWT Auth provider.
http:BearerAuthHandler outboundJwtAuthHandler = new(outboundJwtAuthProvider);

http:Client httpEndpoint = new("https://localhost:9090", config = {
    auth: {
        authHandler: outboundJwtAuthHandler
    }
});

public function main() {
    // Sets the JWT token into the runtime invocation context mentioning
    // the scheme as `jwt`.
    string token = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJiYWxsZXJ" +
        "pbmEiLCJpc3MiOiJiYWxsZXJpbmEiLCJleHAiOjI4MTg0MTUwMTksImlhdCI6MTUyND" +
        "U3NTAxOSwianRpIjoiZjVhZGVkNTA1ODVjNDZmMmI4Y2EyMzNkMGMyYTNjOWQiLCJhd" +
        "WQiOlsiYmFsbGVyaW5hIiwiYmFsbGVyaW5hLm9yZyIsImJhbGxlcmluYS5pbyJdLCJz" +
        "Y29wZSI6ImhlbGxvIn0.bNoqz9_DzgeKSK6ru3DnKL7NiNbY32ksXPYrh6Jp0_O3ST7" +
        "WfXMs9WVkx6Q2TiYukMAGrnMUFrJnrJvZwC3glAmRBrl4BYCbQ0c5mCbgM9qhhCjC1t" +
        "BA50rjtLAtRW-JTRpCKS0B9_EmlVKfvXPKDLIpM5hnfhOin1R3lJCPspJ2ey_Ho6fDh" +
        "sKE3DZgssvgPgI9PBItnkipQ3CqqXWhV-RFBkVBEGPDYXTUVGbXhdNOBSwKw5ZoVJrC" +
        "UiNG5XD0K4sgN9udVTi3EMKNMnVQaq399k6RYPAy3vIhByS6QZtRjOG8X93WJw-9GLi" +
        "Hvcabuid80lnrs2-mAEcstgiHVw";
    runtime:getInvocationContext().authenticationContext.scheme = "jwt";
    runtime:getInvocationContext().authenticationContext.authToken = token;

    // Sends a `GET` request to the specified endpoint.
    var response = httpEndpoint->get("/hello/sayHello");
    if (response is http:Response) {
        var result = response.getTextPayload();
        log:printInfo((result is error) ? "Failed to retrieve payload."
                                        : result);
    } else {
        log:printError("Failed to call the endpoint.", err = response);
    }
}

// Defines the sample backend service, which is secured with JWT Auth
// authentication.
jwt:InboundJwtAuthProvider inboundJwtAuthProvider = new({
    issuer: "ballerina",
    audience: "ballerina.io",
    certificateAlias: "ballerina",
    trustStore: {
        path: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
        password: "ballerina"
    }
});
http:BearerAuthHandler inboundJwtAuthHandler = new(inboundJwtAuthProvider);
listener http:Listener ep = new(9090, config = {
    auth: {
        authHandlers: [inboundJwtAuthHandler]
    },
    secureSocket: {
        keyStore: {
            path: "${ballerina.home}/bre/security/ballerinaKeystore.p12",
            password: "ballerina"
        }
    }
});

service hello on ep {
    resource function sayHello(http:Caller caller, http:Request req) {
        error? result = caller->respond("Hello, World!!!");
        if (result is error) {
            log:printError("Error in responding to caller", err = result);
        }
    }
}
