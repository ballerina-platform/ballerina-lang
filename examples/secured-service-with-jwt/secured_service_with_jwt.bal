import ballerina/http;
import ballerina/jwt;
import ballerina/log;

// Creates an inbound JWT authentication provider with the relevant
// configurations.
jwt:InboundJwtAuthProvider jwtAuthProvider = new({
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

// Creates a Bearer Auth handler with the created JWT Auth provider.
http:BearerAuthHandler jwtAuthHandler = new(jwtAuthProvider);

// The endpoint used here is the `http:Listener`. The JWT authentication
// handler is set to this endpoint using the `authHandlers` attribute.
// It is optional to override the authentication and authorization at the
// service and resource levels.
listener http:Listener ep = new(9090, config = {
    auth: {
        authHandlers: [jwtAuthHandler]
    },
    // The secure hello world sample uses HTTPS.
    secureSocket: {
        keyStore: {
            path: "${ballerina.home}/bre/security/ballerinaKeystore.p12",
            password: "ballerina"
        }
    }
});

@http:ServiceConfig {
    basePath: "/hello"
}
// The Auth configuration comprises of two parts -
// authentication & authorization.
// Authentication can be disabled by setting the `enabled: false` flag.
// Authorization is based on scopes. A scope maps to one or more groups.
// For a user to access a resource, the user should be in the same groups as
// the scope.
// To specify one or more scope of a resource, the annotation attribute
// `scopes` can be used.
service echo on ep {
    @http:ResourceConfig {
        methods: ["GET"],
        path: "/sayHello",
        auth: {
            scopes: ["hello"],
            enabled: true
        }
    }
    // The authentication and authorization settings can be overridden at
    // the resource level.
    // The hello resource would inherit the `enabled: true` flag from the
    // service level, which is set automatically.
    // The scope of the resource is defined as "hello".
    resource function hello(http:Caller caller, http:Request req) {
        error? result = caller->respond("Hello, World!!!");
        if (result is error) {
            log:printError("Error in responding to caller", result);
        }
    }
}
