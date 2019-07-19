## Module overview

This module provides utility methods for obtaining reflective information about the Ballerina runtime.

## Samples

### Get service annotations

The sample below shows how to retrieve all the annotations of a service:

```ballerina
@http:ServiceConfig { basePath: "/helloWorld" }
service hello on new http:Listener(9090) {

    resource function hello(http:Caller caller, http:Request req) {
        http:Response res = new;
        res.setTextPayload("hello world");
        var result = caller->respond(res);
    }
}

reflect:annotationData[] annotations = reflect:getServiceAnnotations(hello); 
string annoName = annotations[0].name; // E.g. “ServiceConfig”
string annoPkg = annotations[0].moduleName; // E.g. “ballerina.http”

```
