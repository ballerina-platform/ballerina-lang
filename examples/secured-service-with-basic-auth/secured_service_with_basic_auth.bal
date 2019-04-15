import ballerina/auth;
import ballerina/http;
import ballerina/log;

// Create a Basic authentication handler with the relevant configuration
// parameters.
auth:ConfigAuthStoreProvider basicAuthProvider = new;
http:BasicAuthnHandler basicAuthnHandler = new(basicAuthProvider);

// The endpoint used here is `http:Listener`, which by default tries to
// authenticate and authorize each request. The developer has the option to
// override the authentication and authorization at the service level and
// resource level.
listener http:Listener ep = new(9090, config = {
    auth: {
        authnHandlers: [basicAuthnHandler]
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
    basePath: "/hello",
    auth: {
        scopes: ["scope1"]
    }
}
// Auth configuration comprises of two parts - authentication & authorization.
// Authentication can be disabled by setting the `enabled: flag` annotation
// attribute, if needed.
// Authorization is based on scopes, where a scope maps to one or more groups.
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
    // The authentication and authorization settings can be overridden at
    // resource level.
    // The hello resource would inherit the `enabled: true` flag from the
    // service level which is set automatically, and override the scope
    // defined in the service level (i.e., scope1) with scope2.
    resource function hello(http:Caller caller, http:Request req) {
        error? result = caller->respond("Hello, World!!!");
        if (result is error) {
            log:printError("Error in responding to caller", err = result);
        }
    }
}
