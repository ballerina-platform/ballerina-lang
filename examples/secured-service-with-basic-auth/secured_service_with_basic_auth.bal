import ballerina/auth;
import ballerina/http;
import ballerina/log;

// Creates a Basic Auth header handler with the relevant configurations.
auth:InboundBasicAuthProvider basicAuthProvider = new;
http:BasicAuthHandler basicAuthHandler = new(basicAuthProvider);

// The endpoint used here is the `http:Listener`, which by default tries to
// authenticate and authorize each request. The Basic Authentication handler is
// set to this endpoint using the `authHandlers` attribute. It is optional to
// override the authentication and authorization at the service level and/or
// resource level.
listener http:Listener ep = new(9090, config = {
    auth: {
        authHandlers: [basicAuthHandler]
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
    basePath: "/hello",
    auth: {
        scopes: ["scope1"]
    }
}
// The Auth configuration comprises of two parts -
// authentication & authorization.
// Authentication can be disabled by setting the `enabled: false` annotation
// attribute.
// Authorization is based on scopes. A scope maps to one or more groups.
// For a user to access a resource, the user should be in the same groups as
// the scope.
// To specify one or more scopes of a resource, the `scopes` annotation
// attribute can be used.
service echo on ep {

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/sayHello",
        auth: {
            scopes: ["scope2"]
        }
    }
    // The authentication and authorization settings can be overridden at the
    // resource level.
    // The hello resource would inherit the `enabled: true` flag from the
    // service level, which is set automatically. The service level scope
    // (i.e., scope1) will be overridden by the scope defined in the resource
    // level (i.e., scope2).
    resource function hello(http:Caller caller, http:Request req) {
        error? result = caller->respond("Hello, World!!!");
        if (result is error) {
            log:printError("Error in responding to caller", result);
        }
    }
}
