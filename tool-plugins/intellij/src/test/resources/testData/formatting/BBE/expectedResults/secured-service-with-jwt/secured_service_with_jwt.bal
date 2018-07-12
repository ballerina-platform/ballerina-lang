import ballerina/http;

// Create a JWT authentication provider with the relevant configuration
// parameters. 
http:AuthProvider jwtAuthProvider = {
    scheme:"jwt",
    issuer:"ballerina",
    audience: "ballerina.io",
    certificateAlias: "ballerina",
    trustStore: {
        path: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
        password: "ballerina"
    }
};
// The endpoint used here is `http:SecureListener`. The JWT authentication
// provider is set to this endpoint using the `authProviders` attribute. The
// developer has the option to override the authentication and authorization
// at the service and resource levels.
endpoint http:SecureListener ep {
    port: 9090,
    authProviders:[jwtAuthProvider],
    // The secure hello world sample uses https.
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
// Auth configuration comprises of two parts - authentication & authorization.
// Authentication can be enabled by setting the `authentication:{enabled:true}`
// flag.
// Authorization is based on scopes, where a scope maps to one or more groups.
// For a user to access a resource, the user should be in the same groups as
// the scope.
// To specify one or more scope of a resource, the annotation attribute
// `scopes` can be used.
service<http:Service> echo bind ep {
    @http:ResourceConfig {
        methods: ["GET"],
        path: "/sayHello",
        authConfig: {
            scopes: ["hello"]
        }
    }
    // The authentication and authorization settings can be overridden at
    // resource level.
    // The hello resource would inherit the `authentication:{enabled:true}` flag
    // from the service level, and define 'hello' as the scope for the resource.
    hello(endpoint caller, http:Request req) {
        http:Response res = new;
        res.setPayload("Hello, World!!!");
        _ = caller->respond(res);
    }
}
