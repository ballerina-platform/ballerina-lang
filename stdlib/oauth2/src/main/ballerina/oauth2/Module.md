## Module Overview

This module provides an OAuth2 authentication provider, which can be used to authenticate the provided credentials against an introspection endpoint.

The `oauth2:OAuth2Provider` is another implementation of the `auth:AuthProvider` interface. This calls an introspection endpoint, validate the token, and performs authentication and authorization.

#### Sample for securing a service with OAuth2

```ballerina
import ballerina/http;
import ballerina/oauth2;

oauth2:IntrospectionServerConfig introspectionServerConfig = {
    url: "https://localhost:9196/oauth2/token/introspect",
    clientConfig: {
        auth: {
            scheme: http:BASIC_AUTH,
            config: {
                username: "3MVG9YDQS5WtC11paU2WcQjBB3L5w4gz52uriT8ksZ3nUVjKvrfQMrU4uvZohTftxStwNEW4cfStBEGRxRL68",
                password: "9205371918321623741"
            }
        }
    }
};

oauth2:OAuth2Provider oauth2Provider = new(introspectionServerConfig);
http:BearerAuthHeaderAuthnHandler oauth2AuthnHandler = new(oauth2Provider);

listener http:Listener listenerEP = new(9116, config = {
    auth: {
        authnHandlers: [oauth2AuthnHandler]
    },
    secureSocket: {
        keyStore: {
            path: "${ballerina.home}/bre/security/ballerinaKeystore.p12",
            password: "ballerina"
        }
    }
});

service echo on listenerEP {
    resource function test(http:Caller caller, http:Request req) {
        checkpanic caller->respond(());
    }
}
```
