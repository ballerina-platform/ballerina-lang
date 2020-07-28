## Module Overview

This module provides the default authentication provider configurations, which can be extended to create new authentication providers and functions to interact with the `auth:InvocationContext`.

#### Invocation Context

The Invocation Context is a data holder, which is created per request and preserved for a single request-response flow. It comprises of auth-related information such as authentication scheme, auth token, and authenticated user's ID, claims, and scopes. 

The following code snippet shows how to access the `auth:InvocationContext` and how to set the data and retrieve them.

Set data to the invocation context.
```ballerina
auth:InvocationContext invocationContext = auth:getInvocationContext();
invocationContext.token = "eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ";
```

Retrieve data from the invocation context.
```ballerina
string? authToken = auth:getInvocationContext()?.token;
```
For information on the operations, which you can perform with this module, see the below **Objects**. For examples on the usage of the operations, see the [Secured Service with Basic Auth Example](https://ballerina.io/swan-lake/learn/by-example/secured-service-with-basic-auth.html) and [Secured Client with Basic Auth Example](https://ballerina.io/swan-lake/learn/by-example/secured-client-with-basic-auth.html).
