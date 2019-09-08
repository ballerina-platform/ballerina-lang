## Module overview

This module provides functions to interact with the runtime, the invocation context and to manage errors.

### Invocation Context

The Invocation Context is a data holder that is created per request and preserved for a single request-response flow
. It comprises of a unique ID, a `Principal` instance that includes user details, attribute map to hold context
 information and an `AuthenticationContext` instance that has the authentication related details if available.

Additionally, there are some utility methods to
* Retrieve the current call stack and the particular call stack frame for an error.
* Halt a `worker` (sleep) for a given period of time.
* Look up properties from the runtime context.

### Samples

The following sample shows how to access the Invocation Context, set data to it and access the same.
```ballerina
import ballerina/io;
import ballerina/runtime;

public function main() {

    // Set data to the Invocation Context.
    runtime:InvocationContext invocationContext = runtime:getInvocationContext();
    runtime:Principal? principal = invocationContext["principal"];
    if (principal is runtime:Principal) {
        // Set the username as ‘tom’.
        principal["username"] = "tom";

        // Set claims.
        map<any> claims = { email: "tom@ballerina.com", org: "wso2" };
        principal.claims = claims;

        // Set scopes.
        string[] scopes = ["email", "profile"];
        principal.scopes = scopes;
    }

    runtime:AuthenticationContext? authContext = invocationContext["authenticationContext"];
    if (authContext is runtime:AuthenticationContext) {
        // Set auth scheme.
        authContext.scheme = "jwt";

        // Set auth token.
        authContext.authToken = "abc.pqr.xyz";
    }

    // Retrieve data from the invocation context.
    runtime:InvocationContext invocationContext1 = runtime:getInvocationContext();
    runtime:Principal? principal1 = invocationContext1["principal"];
    if (principal1 is runtime:Principal) {
        // Retrieve user name.
        string? userName = principal1["username"];
        io:println(userName);

        // Retrieve claims.
        map<any>? retrievedClaims = principal1["claims"];
        io:println(retrievedClaims);

        // Retrieve scopes.
        string[]? retrievedScopes = principal1["scopes"];
        io:println(retrievedScopes);
    }

    runtime:AuthenticationContext? authContext1 = invocationContext1["authenticationContext"];
    if (authContext1 is runtime:AuthenticationContext) {
        // Retrieve auth scheme.
        string? authScheme = authContext1["scheme"];
        io:println(authScheme);

        // Retrieve auth token.
        string? token = authContext1["authToken"];
        io:println(token);
    }
}
```

The following sample shows how to access the call stack and how to trap an error.

```ballerina
import ballerina/io;
import ballerina/runtime;

public function main() {
    // Print the current call stack.
    io:println(runtime:getCallStack());
    var errorMessage = trap getError();
    if (errorMessage is error) {
        io:println(errorMessage.reason());
    }
}

function getError() {
    panicWithError();
}

function panicWithError() {
    // Create an error with a reason.
    error e = error("error occurred");
    panic e;
}
```
