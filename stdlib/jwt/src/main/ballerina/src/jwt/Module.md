This module provides an inbound and outbound JWT authentication provider, which can be used to authenticate using a JWT and the functionality related to issuing and validating JWT.

### Inbound JWT Auth Provider

The `jwt:InboundJwtAuthProvider` is another implementation of the `auth:InboundAuthProvider` interface, which authenticates by validating a JWT.

```ballerina
jwt:InboundJwtAuthProvider jwtAuthProvider = new({
    issuer: "example",
    audience: "ballerina",
    trustStoreConfig: {
        certificateAlias: "ballerina",
        trustStore: {
            path: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
            password: "ballerina"
        }
    }
});
```

### Outbound JWT Auth Provider

The `jwt:OutboundJwtAuthProvider` is another implementation of the `auth:OutboundAuthProvider` interface, which used to authenticate with external endpoint by generating a JWT.

```ballerina
jwt:OutboundJwtAuthProvider jwtAuthProvider = new({
    issuer: "example",
    audience: ["ballerina"],
    keyStoreConfig: {
        keyAlias: "ballerina",
        keyPassword: "ballerina",
        keyStore: {
            path: "${ballerina.home}/bre/security/ballerinaKeystore.p12",
            password: "ballerina"
        }
    }
});
```

For information on the operations, which you can perform with this module, see the below **Functions**. For examples on the usage of the operations, see [JWT Token Validation](https://ballerina.io/v1-2/learn/by-example/jwt-issue-validate.html), [Secured Client with JWT Auth](https://ballerina.io/v1-2/learn/by-example/secured-client-with-jwt-auth.html), [Secured Service with JWT Auth](https://ballerina.io/v1-2/learn/by-example/secured-service-with-jwt-auth.html).
