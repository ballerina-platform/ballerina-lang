import ballerina/net.http;
import ballerina/io;
import ballerina/net.http.endpoints;
import ballerina/auth;

// The endpoint used here is 'endpoints:ApiEndpoint', which by default tries to authenticate and authorize each request.
// The developer has the option to override the authentication and authorization at service and resource level.
endpoint endpoints:ApiEndpoint ep {
    port:9090,
    // The secure hello world sample uses https.
    secureSocket:
    {
        keyStore:{
          filePath:"${ballerina.home}/bre/security/ballerinaKeystore.p12",
          password:"ballerina"
        },
        trustStore:{
          filePath:"${ballerina.home}/bre/security/ballerinaTruststore.p12",
          password:"ballerina"
       }
    }
};

@http:ServiceConfig {
    basePath:"/hello"
}
// Auth configuration comprises of two parts - authentication and authorization.
// Authentication can be enabled by setting 'authentication:{enabled:true}' annotation attribute.
// Authorization is based on scopes, where a scope maps to one or more groups.
// For a user to access a resource, the user should be in the same groups as the scope.
// To specify a scope of a resource, the annotation attribute 'scope' would be used.
@auth:Config {
    authentication:{enabled:true},
    scope:"xxx"
}
service<http:Service> echo bind ep {
    @http:ResourceConfig {
        methods:["GET"],
        path:"/sayHello"
    }
    // The authentication and authorization settings can be overridden at resource level.
    // The hello resource would inherit the authentication:{enabled:true} flag from the
    // service level, and override scope defined in service level (xxx) with scope2.
    @auth:Config {
        scope:"scope2"
    }
    hello (endpoint client, http:Request req) {
        http:Response res = {};
        res.setStringPayload("Hello, World!!!");
        _ = client -> respond(res);
    }
}
