## Module Overview
This module offers an annotation based OpenShift extension implementation for ballerina. 

### Annotation Usage Sample:

```
import ballerina/http;
import ballerina/log;
import ballerina/kubernetes;
import ballerina/openshift;

@kubernetes:Service {}
@openshift:Route {
    host: "www.oc-example.com"
}
listener http:Listener helloEP = new(9090);

@kubernetes:Deployment {
    namespace: "hello-api",
    registry: "172.30.1.1:5000",
    image: "hello-service:v1.0",
    buildImage: false,
    buildExtension: openshift:BUILD_EXTENSION_OPENSHIFT
}
@http:ServiceConfig {
    basePath: "/hello"
}
service hello on helloEP {
    @http:ResourceConfig {
        methods: ["GET"],
        path: "/{user}"
    }
    resource function sayHello(http:Caller caller, http:Request request, string user) {
        string payload = string `Hello ${<@untainted string> user}!`;
        var responseResult = caller->respond(payload);
        if (responseResult is error) {
            error err = responseResult;
            log:printError("Error sending response", err);
        }
    }
}
```
