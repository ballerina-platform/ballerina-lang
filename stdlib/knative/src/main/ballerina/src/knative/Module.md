## Module Overview

This module offers an annotation based Knative extension implementation for ballerina.

### Annotation Usage Sample:

```ballerina
import ballerina/http;
import ballerina/log;
import ballerina/knative;

@knative:Service {
    name:"hello"
}
listener http:Listener helloEP = new(9090);

@http:ServiceConfig {
    basePath: "/helloWorld"
}
service helloWorld on helloEP {
    resource function sayHello(http:Caller caller, http:Request request) {
        var responseResult = caller->respond("Hello, World from service helloWorld ! ");
        if (responseResult is error) {
            log:printError("error responding", responseResult);
        }
    }
}
```
