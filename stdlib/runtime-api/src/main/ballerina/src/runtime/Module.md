This module provides functions to interact with the runtime, the invocation context and to manage errors.

### Invocation Context

The Invocation Context is a data holder that is created per request and preserved for a single request-response flow. It comprises of a unique ID, a `runtime:Principal` instance that includes user details, an `runtime:AuthenticationContext` instance that has the authentication related details if available, and an attribute map to hold context information.

The following code snippet shows how to access the `runtime:InvocationContext` and the set of data.
```ballerina
runtime:InvocationContext invocationContext = runtime:getInvocationContext();

runtime:Principal? principal = invocationContext["principal"];
if (principal is runtime:Principal) {
    // Set the username as ‘tom’.
    principal["username"] = "tom";

    // Set the claims.
    map<any> claims = { email: "tom@ballerina.com", org: "wso2" };
    principal.claims = claims;

    // Set scopes.
    string[] scopes = ["email", "profile"];
    principal.scopes = scopes;
}

runtime:AuthenticationContext? authContext = invocationContext["authenticationContext"];
if (authContext is runtime:AuthenticationContext) {
    // Set the auth scheme.
    authContext.scheme = "jwt";

    // Set the auth token.
    authContext.authToken = "abc.pqr.xyz";
}
```

The following code snippet shows how to access the `runtime:InvocationContext` and get the data.
```ballerina
runtime:InvocationContext invocationContext = runtime:getInvocationContext();
runtime:Principal? principal = invocationContext["principal"];
if (principal is runtime:Principal) {
    // Retrieve the user name.
    string? userName = principal["username"];
    io:println(userName);

    // Retrieve the claims.
    map<any>? claims = principal["claims"];
    io:println(claims);

    // Retrieve the scopes.
    string[]? scopes = principal["scopes"];
    io:println(scopes);
}

runtime:AuthenticationContext? authContext = invocationContext["authenticationContext"];
if (authContext is runtime:AuthenticationContext) {
    // Retrieve the auth scheme.
    string? authScheme = authContext["scheme"];
    io:println(authScheme);

    // Retrieve the auth token.
    string? token = authContext["authToken"];
    io:println(token);
}
```

For information on the operations, which you can perform with this module, see the below **Functions**.
