import ballerina/config;
import ballerina/http;
import ballerina/log;

// Define the basic auth client endpoint to call the backend services.
// Basic authentication is enabled by setting the `scheme: http:BASIC_AUTH`
// The `username` and `password` should be specified as needed.
endpoint http:Client httpEndpoint {
    url: "https://localhost:9090",
    auth: {
        scheme: http:BASIC_AUTH,
        username: "tom",
        password: "1234"
    }
};

public function main() {
    // This defines the authentication credentials of the HTTP service.
    config:setConfig("b7a.users.tom.password", "1234");

    // Send a `GET` request to the specified endpoint.
    var response = httpEndpoint->get("/hello/sayHello");
    match response {
        http:Response resp => log:printInfo(resp.getPayloadAsString() but {error => "Failed to retrieve payload."});
        error err => log:printError("Failed to call the endpoint.");
    }
}

// Create a basic authentication provider with the relevant configurations.
endpoint http:SecureListener ep {
    port: 9090,
    secureSocket: {
        keyStore: {
            path: "${ballerina.home}/bre/security/ballerinaKeystore.p12",
            password: "ballerina"
        },
        trustStore: {
            path: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
            password: "ballerina"
        }
    }
};

@http:ServiceConfig {
    basePath: "/hello",
    authConfig: {
        authentication: { enabled: true }
    }
}
service<http:Service> echo bind ep {

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/sayHello"
    }
    hello(endpoint caller, http:Request req) {
        http:Response res = new;
        res.setPayload("Hello, World!!!");
        _ = caller->respond(res);
    }
}
