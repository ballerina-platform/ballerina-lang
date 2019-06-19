import ballerina/auth;
import ballerina/config;
import ballerina/http;
import ballerina/log;

// Define the basic auth client endpoint to call the backend services.
// Basic authentication is enabled by setting the `scheme: http:BASIC_AUTH`
// The `username` and `password` should be specified as needed.
http:Client httpEndpoint = new("https://localhost:9090", config = {
    auth: {
        scheme: http:BASIC_AUTH,
        config: {
            username: "tom",
            password: "1234"
        }
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

// Create a Basic authentication handler with the relevant configurations.
auth:ConfigAuthStoreProvider basicAuthProvider = new;
http:BasicAuthHeaderAuthnHandler basicAuthnHandler = new(basicAuthProvider);

listener http:Listener ep  = new(9090, config = {
    auth: {
        authnHandlers: [basicAuthnHandler]
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
