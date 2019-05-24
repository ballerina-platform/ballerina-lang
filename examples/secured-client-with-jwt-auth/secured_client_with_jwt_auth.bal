import ballerina/http;
import ballerina/jwt;
import ballerina/log;
import ballerina/runtime;

// Define the JWT auth client endpoint to call the backend services.
// JWT authentication is enabled by setting the `scheme: http:JWT_AUTH`
http:Client httpEndpoint = new("https://localhost:9090", config = {
    auth: {
        scheme: http:JWT_AUTH
    }
});

public function main() {
    // Set the JWT token into runtime invocation context mentioning
    // scheme as `jwt`
    string token = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJiYWxsZXJ" +
        "pbmEiLCJpc3MiOiJiYWxsZXJpbmEiLCJleHAiOjI4MTg0MTUwMTksImlhdCI6MTUyND" +
        "U3NTAxOSwianRpIjoiZjVhZGVkNTA1ODVjNDZmMmI4Y2EyMzNkMGMyYTNjOWQiLCJhdW" +
        "QiOlsiYmFsbGVyaW5hIiwiYmFsbGVyaW5hLm9yZyIsImJhbGxlcmluYS5pbyJdLCJzY" +
        "29wZSI6ImhlbGxvIn0.bNoqz9_DzgeKSK6ru3DnKL7NiNbY32ksXPYrh6Jp0_O3ST7W" +
        "fXMs9WVkx6Q2TiYukMAGrnMUFrJnrJvZwC3glAmRBrl4BYCbQ0c5mCbgM9qhhCjC1tB" +
        "A50rjtLAtRW-JTRpCKS0B9_EmlVKfvXPKDLIpM5hnfhOin1R3lJCPspJ2ey_Ho6fDhs" +
        "KE3DZgssvgPgI9PBItnkipQ3CqqXWhV-RFBkVBEGPDYXTUVGbXhdNOBSwKw5ZoVJrCU" +
        "iNG5XD0K4sgN9udVTi3EMKNMnVQaq399k6RYPAy3vIhByS6QZtRjOG8X93WJw-9GLiH" +
        "vcabuid80lnrs2-mAEcstgiHVw";
    runtime:getInvocationContext().authenticationContext.scheme = "jwt";
    runtime:getInvocationContext().authenticationContext.authToken = token;

    // Send a `GET` request to the specified endpoint.
    var response = httpEndpoint->get("/hello/sayHello");
    if (response is http:Response) {
        var result = response.getTextPayload();
        log:printInfo((result is error) ? "Failed to retrieve payload."
                                        : result);
    } else {
        log:printError("Failed to call the endpoint.", err = response);
    }
}

// Create a JWT authentication provider with the relevant configurations.
jwt:JWTAuthProvider jwtAuthProvider = new({
    issuer: "ballerina",
    audience: ["ballerina.io"],
    certificateAlias: "ballerina",
    trustStore: {
        path: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
        password: "ballerina"
    }
});

// Create a JWT authentication handler with the created JWT auth provider.
http:JwtAuthnHandler jwtAuthnHandler = new(jwtAuthProvider);

listener http:Listener ep = new(9090, config = {
    auth: {
        authnHandlers: [jwtAuthnHandler]
    },
    secureSocket: {
        keyStore: {
            path: "${ballerina.home}/bre/security/ballerinaKeystore.p12",
            password: "ballerina"
        }
    }
});

@http:ServiceConfig {
    basePath: "/hello",
    auth: {
        enabled: true
    }
}
service echo on ep {

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/sayHello"
    }
    resource function hello(http:Caller caller, http:Request req) {
        error? result = caller->respond("Hello, World!!!");
        if (result is error) {
            log:printError("Error in responding to caller", err = result);
        }
    }
}
