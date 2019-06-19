import ballerina/auth;
import ballerina/config;
import ballerina/http;
import ballerina/log;

// Define the Basic auth client endpoint to call the backend services.
// Basic authentication is enabled by creating `auth:OutboundBasicAuthProvider`
// with the `username` and `password` passed as a record.
auth:OutboundBasicAuthProvider outboundBasicAuthProvider = new({
    username: "tom",
    password: "1234"
});

// Create a Basic auth handler with the created Basic auth provider.
http:BasicAuthHandler outboundBasicAuthHandler =
                                            new(outboundBasicAuthProvider);

http:Client httpEndpoint = new("https://localhost:9090", config = {
    auth: {
        authHandler: outboundBasicAuthHandler
    }
});

public function main() {
    // This defines the authentication credentials of the HTTP service.
    config:setConfig("b7a.users.tom.password", "1234");

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

// Defines the sample backend service, secured with Basic auth authentication.
auth:InboundBasicAuthProvider inboundBasicAuthProvider = new(());
http:BasicAuthHandler inboundBasicAuthHandler = new(inboundBasicAuthProvider);
listener http:Listener ep  = new(9090, config = {
    auth: {
        authHandlers: [inboundBasicAuthHandler]
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
