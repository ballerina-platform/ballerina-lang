import ballerina/http;
import ballerina/log;
import ballerina/oauth2;

// Creates an inbound OAuth2 authentication provider with the relevant
// configurations of the introspection server.
oauth2:InboundOAuth2Provider oauth2Provider = new({
    url: "https://localhost:9095/oauth2/token/introspect"
});

// Creates a Bearer Auth handler with the created OAuth2 provider.
http:BearerAuthHandler oauth2Handler = new(oauth2Provider);

// The endpoint used here is the `http:Listener`. The OAuth2 handler is
// set to this endpoint using the `authHandlers` attribute.
// It is optional to override the authentication and authorization at the
// service and resource levels.
listener http:Listener ep = new(9090, {
    auth: {
        authHandlers: [oauth2Handler]
    },
    // The secure hello world sample uses HTTPS.
    secureSocket: {
        keyStore: {
            path: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
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
