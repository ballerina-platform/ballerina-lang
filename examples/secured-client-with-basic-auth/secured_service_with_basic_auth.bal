import ballerina/auth;
import ballerina/http;
import ballerina/log;

// Defines the sample backend service, which is secured with Basic Auth
// authentication.
auth:InboundBasicAuthProvider inboundBasicAuthProvider = new;
http:BasicAuthHandler inboundBasicAuthHandler = new(inboundBasicAuthProvider);

listener http:Listener ep  = new(9090, config = {
    auth: {
        authHandlers: [inboundBasicAuthHandler],
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
