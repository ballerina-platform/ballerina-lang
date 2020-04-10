This module provides an inbound OAuth2 authentication provider, which can be used to authenticate the provided credentials against an introspection endpoint and an outbound OAuth2 authentication provider, which can be used to authenticate an external endpoint.

### Inbound OAuth2 Provider

The `oauth2:InboundOAuth2Provider` is an implementation of the `auth:InboundAuthProvider` interface. This calls an introspection endpoint, validates the token, and performs authentication and authorization.

```ballerina
oauth2:IntrospectionServerConfig introspectionServerConfig = {
    url: "https://localhost:9196/oauth2/token/introspect"
};
oauth2:InboundOAuth2Provider oauth2Provider = new(introspectionServerConfig);
```

### Outbound OAuth2 Provider

The `oauth2:OutboundOAuth2Provider` is an implementation of the `auth:OutboundAuthProvider` interface. This is used to call an external endpoint with authentication.

1. Client Credentials Grant Type

```ballerina
oauth2:OutboundOAuth2Provider oauth2Provider1 = new({
    tokenUrl: "https://localhost:9196/oauth2/token",
    clientId: "3MVG9YDQS5WtC11paU2WcQjBB3L",
    clientSecret: "9205371918321623741",
    scopes: ["token-scope1", "token-scope2"]
});
```

2. Password Grant Type

```ballerina
oauth2:OutboundOAuth2Provider oauth2Provider5 = new({
    tokenUrl: "https://localhost:9196/oauth2/token/authorize/header",
    username: "johndoe",
    password: "A3ddj3w",
    clientId: "3MVG9YDQS5WtC11paU2WcQjBB3L",
    clientSecret: "9205371918321623741",
    scopes: ["token-scope1", "token-scope2"]
});
```

3. Direct Token Mode

```ballerina
oauth2:OutboundOAuth2Provider oauth2Provider13 = new({
    accessToken: "2YotnFZFEjr1zCsicMWpAA",
    refreshConfig: {
        refreshUrl: "https://localhost:9196/oauth2/token/refresh",
        refreshToken: "XlfBs91yquexJqDaKEMzVg==",
        clientId: "3MVG9YDQS5WtC11paU2WcQjBB3L",
        clientSecret: "9205371918321623741",
        scopes: ["token-scope1", "token-scope2"]
    }
});
```

For information on the operations, which you can perform with this module, see the below **Functions**. For examples on the usage of the operations, see the [Secured Service with OAuth2 Example](https://ballerina.io/learn/by-example/secured-service-with-oauth2.html) and [Secured Client with OAuth2 Example](https://ballerina.io/learn/by-example/secured-client-with-oauth2.html).
