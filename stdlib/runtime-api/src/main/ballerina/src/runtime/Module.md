## Module overview

This module includes functions to interact with the runtime, the invocation context and to manage callStack element.

## Samples

### Invocation Context

The Invocation Context is a data holder that is created per request and preserved for a single request-response flow.
The Invocation Context comprises of a unique ID, a `Principal` instance that includes user details and an 
 `AuthenticationContext` instance that has the authentication related details if available.
 
 The following sample shows how to access the Invocation Context, set data to it and access the same.
 ```ballerina
 import ballerina/io;
 import ballerina/runtime;
 
 public function main() {
 
     runtime:InvocationContext invocationCtx = runtime:getInvocationContext();
 
     // Set data to the Invocation Context.
 
     // Set the username as ‘tom’.
     invocationCtx.principal.username = "tom";
 
     // Set claims.
     map<any> claims = { email: "tom@ballerina.com", org: "wso2" };
     invocationCtx.principal.claims = claims;
 
     // Set scopes.
     string[] scopes = ["email", "profile"];
     invocationCtx.principal.scopes = scopes;
 
     // Set auth scheme.
     invocationCtx.authenticationContext.scheme = "jwt";
 
     // Set auth token.
     invocationCtx.authenticationContext.authToken = "abc.pqr.xyz";
 
     // Retrieve data from the invocation context.
 
     // Retrieve user name.
     var userName = runtime:getInvocationContext()?.principal?.username;
     io:println(userName);
 
     // Retrieve claims.
     var retrievedClaims = runtime:getInvocationContext()?.principal?.claims;
     io:println(retrievedClaims);
 
     // Retrieve scopes.
     var retrievedScopes = runtime:getInvocationContext()?.principal?.scopes;
     io:println(retrievedScopes);
 
     // retrieve auth scheme.
     var authScheme = runtime:getInvocationContext()?.authenticationContext?.scheme;
     io:println(authScheme);
 
     // Retrieve auth token.
     var token = runtime:getInvocationContext()?.authenticationContext?.authToken;
     io:println(token);
 }
 ```

### CallStack Element

The runtime module includes a utility method to retrieve the current call stack and the particular call stack frame for
 an error. 

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

Additionally, the runtime module also contains utility methods to halt a `worker` (sleep) for a given period of time
  and to look up properties from the runtime context.

Following code snippet shows how to halt the current `worker` for a given time period.
```ballerina
// Sleep the current worker for 5 seconds.
runtime:sleep(5000);
```

Following code snippet shows how to access properties from the runtime. 
```ballerina
// Retrieve the property ‘ballerina version’ from the runtime.
string balVersion = runtime:getProperty("ballerina.version");
```
