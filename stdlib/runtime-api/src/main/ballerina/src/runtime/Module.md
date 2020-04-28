## Module Overview

This module provides functions to interact with the runtime, the invocation context, and to manage errors.

### Invocation Context

The Invocation Context is a data holder, which is created per request and preserved for a single request-response flow. It comprises of a unique ID, a `runtime:Principal` instance, which includes user details, a `runtime:AuthenticationContext`, which has the authentication-related details if available, and an attribute map to hold context information.

The following code snippet shows how to access the `runtime:InvocationContext` and the set of data for the `runtime:Principal` and `runtime:AuthenticationContext`.
```ballerina
runtime:InvocationContext invocationContext = runtime:getInvocationContext();

runtime:Principal? principal = invocationContext["principal"];
if (principal is runtime:Principal) {
    // Set the username as ‘tom’.
    principal["username"] = "tom";
}

runtime:AuthenticationContext? authContext = invocationContext["authenticationContext"];
if (authContext is runtime:AuthenticationContext) {
    // Set the auth scheme.
    authContext.scheme = "jwt";
}
```

The following code snippet shows how to access the `runtime:InvocationContext` and get the data from the `runtime:Principal` and `runtime:AuthenticationContext`,
```ballerina
runtime:InvocationContext invocationContext = runtime:getInvocationContext();
runtime:Principal? principal = invocationContext["principal"];
if (principal is runtime:Principal) {
    // Retrieve the user name.
    string? userName = principal["username"];
}

runtime:AuthenticationContext? authContext = invocationContext["authenticationContext"];
if (authContext is runtime:AuthenticationContext) {
    // Retrieve the auth scheme.
    string? authScheme = authContext["scheme"];
}
```

For information on the operations, which you can perform with this module, see the below **Functions**.
