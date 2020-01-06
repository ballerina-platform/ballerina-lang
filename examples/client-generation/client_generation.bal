import ballerina/http;
import ballerina/log;
import ballerina/openapi;

// Add the `@openapi:ClientEndpoint` annotation to the listener that is to be used for client generation.
@openapi:ClientEndpoint
listener http:Listener helloEp = new(9090);

// Add the `@openapi:ClientConfig {generate: true}` annotation to the service to enable client generation for it.
@openapi:ClientConfig {
    generate: true
}
@http:ServiceConfig {
    basePath: "/sample"
}
service Hello on helloEp {

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/hello"
    }
    resource function hello(http:Caller caller, http:Request req) {
        var result = caller->respond("Hello");
        if (result is error) {
            log:printError("Error when responding", err = result);
        }
    }
}

