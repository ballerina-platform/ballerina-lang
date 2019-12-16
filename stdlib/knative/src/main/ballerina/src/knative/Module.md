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
        http:Response response = new;
        response.setTextPayload("Hello, World from service helloWorld ! ");
        var responseResult = caller->respond(response);
        if (responseResult is error) {
            log:printError("error responding", responseResult);
        }
    }
}
```
