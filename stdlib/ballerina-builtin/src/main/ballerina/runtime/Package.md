## Package overview

This package includes functions to interact with the runtime, the invocation context and to manage errors.

### Invocation Context

The Invocation Context is a data holder which is created per request, and preserved for a single request-response flow. 
The Invocation Context comprises of a unique id, UserPrincipal instance which includes user details and an AuthContext 
instance which has the authentication related details if available.

### Samples

The following sample shows how to access the Invocation Context and set data to it.
```ballerina
import ballerina/runtime;

// set date to the Invocation Context    
// set the username ‘tom’ as the user id
runtime:getInvocationContext().userPrincipal.username = "tom";
// set claims
map claims = {email:"tom@ballerina.com", org:"wso2"};
runtime:getInvocationContext().userPrincipal.claims = claims;
// set scopes
string[] scopes = ["email", "profile"];
runtime:getInvocationContext().userPrincipal.scopes = scopes;
// set auth scheme
runtime:getInvocationContext().authContext.scheme = “jwt”;
// set auth token
runtime:getInvocationContext().authContext.authToken = “abc.pqr.xyz”;

// retrieve data from the invocation context
// retrieve user name
string userName = runtime:getInvocationContext().userPrincipal.username;
// retrieve claims
map claims = runtime:getInvocationContext().userPrincipal.claims;
// retrieve scopes
string[] scopes = runtime:getInvocationContext().userPrincipal.scopes;
// retrieve auth scheme
string authScheme = runtime:getInvocationContext().authContext.scheme;
// retrieve auth token
string token =  runtime:getInvocationContext().authContext.authToken;
```

The following sample shows how to sleep a worker thread for a given time period.
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
    error e = {message:"error 2 occured"};
    throw e;
}

try {
    throwError1();
    
} catch (error e) {
    // prints the call stack frame for the error caught
    io:println(runtime:getErrorCallStackFrame(e));
}
```
