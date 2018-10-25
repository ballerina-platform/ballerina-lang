## Module overview

This module includes functions to interact with the runtime, the invocation context and to manage errors.

### Invocation Context

The Invocation Context is a data holder that is created per request and preserved for a single request-response flow.
The Invocation Context comprises of a unique ID, a `UserPrincipal` instance that includes user details and an 
 `AuthContext` instance that has the authentication related details if available.

### Errors

The runtime module includes the `NullReferenceException` and `IllegalStateException` error types. These two error 
types wrap the `error` type defined in the `ballerina/runtime` module. Furthermore, there are utility methods to 
retrieve the current call stack and the particular call stack frame for an error. 

Additionally, the runtime module also contains utility methods to halt a `worker` (sleep) for a given period of time
  and to look up properties from the runtime context.

### Samples

The following sample shows how to access the Invocation Context, set data to it and access the same.
```ballerina
import ballerina/runtime;
import ballerina/io;

// set data to the Invocation Context
// set the username ‘tom’ as the user name
runtime:getInvocationContext().userPrincipal.username = "tom";
// set claims
map claims = { email: "tom@ballerina.com", org: "wso2" };
runtime:getInvocationContext().userPrincipal.claims = claims;
// set scopes
string[] scopes = ["email", "profile"];
runtime:getInvocationContext().userPrincipal.scopes = scopes;
// set auth scheme
runtime:getInvocationContext().authContext.scheme = "jwt";
// set auth token
runtime:getInvocationContext().authContext.authToken = "abc.pqr.xyz";

// retrieve data from the invocation context
// retrieve user name
string userName = runtime:getInvocationContext().userPrincipal.username;
io:println(userName);
// retrieve claims
map retrievedClaims = <map>runtime:getInvocationContext().userPrincipal.claims;
io:println(retrievedClaims);
// retrieve scopes
string[] retrievedScopes = runtime:getInvocationContext().userPrincipal.scopes;
io:println(retrievedScopes);
// retrieve auth scheme
string authScheme = runtime:getInvocationContext().authContext.scheme;
io:println(authScheme);
// retrieve auth token
string token = runtime:getInvocationContext().authContext.authToken;
io:println(token);
```

The following sample shows how to halt the current `worker` for a given time period.
```ballerina
import ballerina/runtime;

// sleep the current worker for 5 seconds
runtime:sleep(5000);
```

The following sample shows how to access properties from the runtime. 
```ballerina
import ballerina/runtime;

// retrieve the property ‘ballerina version’ from the runtime
runtime:getProperty("ballerina.version");
```

The following sample shows how to create and throw and error and how to access the call stack for the error.
```ballerina
import ballerina/runtime;
import ballerina/io;

// prints the current call stack
io:println(runtime:getCallStack());

function throwError1 () {
    throwError2();
}

function throwError2 () {
    // creates an error with a message
    error e = { message: "error 2 occured" };
    throw e;
}

try {
    throwError1();
} catch (error e) {
    // prints the call stack frame for the error caught
    io:println(runtime:getErrorCallStackFrame(e));
}
```
