## Module overview
This module provides the following code generation capabilities to Ballerina:
1. Generate the Ballerina code for a given OpenAPI definition.
2. Generate the client stub for an existing Ballerina service at build time.
3. Export the OpenAPI definition of a Ballerina service.

The `openapi` command in Ballerina is used for OpenAPI to Ballerina and Ballerina to OpenAPI code generation.
Code generation from OpenAPI to Ballerina can produce `mock services` and `client stubs`.

For build time client stub generation, annotation support is provided.

### Mock Service from OpenAPI
`ballerina openapi gen-service <openapifile>
    [-m <module-name>|--module <module-name>] 
    [-o <path>|--output<path>]`

Generates a Ballerina service for the OpenAPI file.

This generated service is a mock version of the actual service. Generated sources contain the service definition in `gen/` and the resource implementation file in the module root directory with the suffix `_impl`. The `_impl` file is not overwritten by code regeneration.

### Client Stub from OpenAPI
`ballerina openapi gen-client <openapifile>
    [-m <module-name>|--module <module-name>] 
    [-o <path>|--output<path>]`
    
Generates a Ballerina client stub for the service defined in a OpenAPI file.

This client can be used in client applications to call the service defined in the OpenAPI file.

### Service to OpenAPI Export
`ballerina openapi export <balfile>
    [-o <path>|--output<path>]
    [-s <servicename>|--service <servicename>]`

Export the Ballerina service to a definition of OpenApi Specification 3.0.
For the export to work properly, the input Ballerina service should be defined using basic service and resource level HTTP annotations.

### Client Stub for Service
Generates a Ballerina client stub to communicate with a Ballerina service.

All endpoint(s) that are used for client stub generation should be marked with the `@openapi:ClientEndpoint` annotation. If not, there might be errors during client stub generation. Endpoints that are not marked with this annotation are not picked for client stub generation.
The `@openapi:ClientConfig { generate: true }` annotation is used to enable or disable client stub generation per service.

## Samples
### Mock Service From OpenAPI
`ballerina openapi gen-service hello_service.yaml -p hello_service`

### Client Stub From OpenAPI
`ballerina openapi gen-client hello_service.yaml -p hello_client`

### OpenAPI From Service
`ballerina openapi export hello_service.bal`

### Client stub From Service
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
            log:printError("Error when responding", err = result);
        }
    }
}
```
