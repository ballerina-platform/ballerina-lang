## Module overview

This module provides the following code generation capabilities.
1. Generate the Ballerina code for a given OpenAPI definition.
2. Generate the client stub for an existing Ballerina service at build time.
3. Export the OpenAPI definition of a Ballerina service.

The `openapi` command in Ballerina is used for OpenAPI to Ballerina and Ballerina to OpenAPI code generation.
Code generation from OpenAPI to Ballerina can produce `ballerina mock services` and `ballerina client stubs`.

For build time client stub generation, annotation support is provided.

### Mock service from OpenAPI
`ballerina openapi gen-service <moduleName>:<serivceName> 
    <openapi_contract>
    [-c: copy-contract] 
    [-o: outputFile]`

Generates a Ballerina service for the OpenAPI file.

This generated service is a mock version of the actual Ballerina service. Generated sources contain the service definition in `src/<module-name>/` and the contract will be available in `src/<module-name>/resources`. 

### Client stub from OpenAPI
`ballerina openapi gen-client 
    [moduleName]:clientName 
    openapi-contract 
    -o[output directory name]`
    
Generates a Ballerina client stub for the service defined in a OpenAPI file.

This client can be used in client applications to call the service defined in the OpenAPI file.

### Service to OpenAPI export
`ballerina openapi gen-contract 
    [moduleName]:serviceName 
    [-i: ballerinaFile] 
    [-o: contractFile] 
    [-s: skip-bind]`

Export the Ballerina service to a definition of OpenApi Specification 3.0.
For the export to work properly, the input Ballerina service should be defined using basic service and resource level HTTP annotations.

### Client stub for service
Generates a Ballerina client stub to communicate with a Ballerina service.

All endpoint(s) that are used for client stub generation should be marked with the `@openapi:ClientEndpoint` annotation. If not, there might be errors during client stub generation. Endpoints that are not marked with this annotation are not picked for client stub generation.
The `@openapi:ClientConfig { generate: true }` annotation is used to enable or disable client stub generation per service.

## Samples
### Mock service from OpenAPI
`ballerinna openapi gen-service helloworld:helloService hello_service.yaml`

### Client stub from OpenAPI
`ballerina openapi gen-client hello_client hello_service.yaml`

### OpenAPI from service
`ballerina openapi gen-contract hello_service.bal`

### Client stub from service
```ballerina
import ballerina/http;
import ballerina/log;
import ballerina/openapi;

// Define this endpoint as a selected endpoint for client generation.
@openapi:ClientEndpoint
listener http:Listener helloEp = new(9090);

// Enable client code generation for this service.
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
        http:Response res = new;
        res.setPayload("Hello");
        var result = caller->respond(res);
        if (result is error) {
            log:printError("Error when responding", result);
        }
    }
}
```
