import ballerina/http;
import ballerina/jwt;
import ballerina/log;

// Defines the sample backend service, which is secured with JWT Auth
// authentication.
jwt:InboundJwtAuthProvider inboundJwtAuthProvider = new({
    issuer: "ballerina",
    audience: "ballerina.io",
    trustStoreConfig: {
        certificateAlias: "ballerina",
        trustStore: {
            path: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
            password: "ballerina"
        }
    }
});
http:BearerAuthHandler inboundJwtAuthHandler = new(inboundJwtAuthProvider);
listener http:Listener ep = new(9090, config = {
    auth: {
        authHandlers: [inboundJwtAuthHandler],
        scopes: ["hello"]
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
            log:printError("Error in responding to caller", result);
        }
    }
}

