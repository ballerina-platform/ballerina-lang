import ballerina/auth;
import ballerina/http;
import ballerina/log;

// Create a JWT authentication provider with the relevant configuration
// parameters. 
auth:JWTAuthProvider jwtAuthProvider = new({
    issuer: "ballerina",
    audience: ["ballerina.io"],
    certificateAlias: "ballerina",
    trustStore: {
        path: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
        password: "ballerina"
    }
});

// Create a JWT authentication handler with the created JWT auth provider
http:JwtAuthnHandler jwtAuthnHandler = new(jwtAuthProvider);

// The endpoint used here is `http:Listener`. The JWT authentication
// handler is set to this endpoint using the `authnHandlers` attribute. The
// developer has the option to override the authentication and authorization
// at the service and resource levels.
listener http:Listener ep = new(9090, config = {
    auth: {
        authnHandlers: [jwtAuthnHandler]
    },
    // The secure hello world sample uses https.
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
// Auth configuration comprises of two parts - authentication & authorization.
// Authentication can be disabled by setting the `enabled: false` flag, if needed.
// Authorization is based on scopes, where a scope maps to one or more groups.
// For a user to access a resource, the user should be in the same groups as
// the scope.
// To specify one or more scope of a resource, the annotation attribute
// `scopes` can be used.
service echo on ep {
    @http:ResourceConfig {
        methods: ["GET"],
        path: "/sayHello",
        auth: {
            scopes: ["hello"]
        }
    }
    // The authentication and authorization settings can be overridden at
    // resource level.
    // The hello resource would inherit the `enabled: true` flag from the
    // service level which is set automatically, and define `hello` as the
    // scope for the resource.
    resource function hello(http:Caller caller, http:Request req) {
        error? result = caller->respond("Hello, World!!!");
        if (result is error) {
            log:printError("Error in responding to caller", err = result);
        }
    }
}
