## Module overview

This module includes functions to interact with the runtime, the invocation context and to manage errors.

### Invocation Context

The Invocation Context is a data holder that is created per request and preserved for a single request-response flow.
The Invocation Context comprises of a unique ID, a `UserPrincipal` instance that includes user details and an 
 `AuthenticationContext` instance that has the authentication related details if available.

### Errors

The runtime module includes the `NullReferenceException` and `IllegalStateException` error types. These two error 
types wrap the `error` type defined in the `ballerina/runtime` module. Furthermore, there are utility methods to 
retrieve the current call stack and the particular call stack frame for an error. 

Additionally, the runtime module also contains utility methods to halt a `worker` (sleep) for a given period of time
  and to look up properties from the runtime context.

### Samples

The following sample shows how to access the Invocation Context, set data to it and access the same.
```ballerina
import ballerina/io;
import ballerina/runtime;

// Set data to the Invocation Context.

// Set the username as ‘tom’.
runtime:getInvocationContext().principal.username = "tom";

// Set claims.
map<any> claims = { email: "tom@ballerina.com", org: "wso2" };
runtime:getInvocationContext().principal.claims = claims;

// Set scopes.
string[] scopes = ["email", "profile"];
runtime:getInvocationContext().principal.scopes = scopes;

// Set auth scheme.
runtime:getInvocationContext().authenticationContext.scheme = "jwt";

// Set auth token.
runtime:getInvocationContext().authenticationContext.authToken = "abc.pqr.xyz";

// Retrieve data from the invocation context.

// Retrieve user name.
string userName = runtime:getInvocationContext().principal.username;
io:println(userName);

// Retrieve claims.
map<any> retrievedClaims = runtime:getInvocationContext().principal.claims;
io:println(retrievedClaims);

// Retrieve scopes.
string[] retrievedScopes = runtime:getInvocationContext().principal.scopes;
io:println(retrievedScopes);

// retrieve auth scheme.
string authScheme = runtime:getInvocationContext().authenticationContext.scheme;
io:println(authScheme);

// Retrieve auth token.
string token = runtime:getInvocationContext().authenticationContext.authToken;
io:println(token);
```

The following sample shows how to halt the current `worker` for a given time period.
```ballerina
import ballerina/runtime;

// Sleep the current worker for 5 seconds.
runtime:sleep(5000);
```

The following sample shows how to access properties from the runtime. 
```ballerina
import ballerina/runtime;

// Retrieve the property ‘ballerina version’ from the runtime.
runtime:getProperty("ballerina.version");
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
    error e = error("error occured");
    panic e;
}
```
