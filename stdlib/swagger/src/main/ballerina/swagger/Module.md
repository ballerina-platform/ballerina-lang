## Module overview
This module provides the following code generation capabilities to Ballerina:
1. Generate the Ballerina code for a given Swagger definition.
2. Generate the client stub for an existing Ballerina service at build time.
3. Export the Swagger definition of a Ballerina service.

The `swagger` command in Ballerina is used for Swagger to Ballerina and Ballerina to Swagger code generation.
Code generation from Swagger to Ballerina can produce `mock services` and `client stubs`.

For build time client stub generation, annotation support is provided.

### Mock Service from Swagger
`ballerina swagger mock <swaggerfile> 
    [-m <module-name>|--module <module-name>] 
    [-o <path>|--output<path>]`

Generates a Ballerina service for the Swagger file.

This generated service is a mock version of the actual service. Generated sources contain the service definition in `gen/` and the resource implementation file in the module root directory with the suffix `_impl`. The `_impl` file is not overwritten by code regeneration.

### Client Stub from Swagger
`ballerina swagger client <swaggerfile> 
    [-m <module-name>|--module <module-name>] 
    [-o <path>|--output<path>]`
    
Generates a Ballerina client stub for the service defined in a Swagger file.

This client can be used in client applications to call the service defined in the Swagger file.

### Service to Swagger Export
`ballerina swagger export <balfile> 
    [-o <path>|--output<path>]
    [-s <servicename>|--service <servicename>]`

Export the Ballerina service to a definition of OpenApi Specification 3.0.
For the export to work properly, the input Ballerina service should be defined using basic service and resource level HTTP annotations.

### Client Stub for Service
Generates a Ballerina client stub to communicate with a Ballerina service.

All endpoint(s) that are used for client stub generation should be marked with the `@swagger:ClientEndpoint` annotation. If not, there might be errors during client stub generation. Endpoints that are not marked with this annotation are not picked for client stub generation.
The `@swagger:ClientConfig {generate: true}` annotation is used to enable or disable client stub generation per service.

## Samples
### Mock Service From Swagger
`ballerina swagger mock hello_service.yaml -p hello_service`

### Client Stub From Swagger
`ballerina swagger client hello_service.yaml -p hello_client`

### Swagger From Service
`ballerina swagger export hello_service.bal`

### Client stub From Service
```ballerina
import ballerina/io;
import ballerina/http;
import ballerina/swagger;
import ballerina/log;

// Define this endpoint as a selected endpoint for client generation.
@swagger:ClientEndpoint
endpoint http:Listener helloEp {
    port: 9090
};

// Enable client code generation for this service.
@swagger:ClientConfig {
    generate: true
}
@http:ServiceConfig {
    basePath: "/sample"
}
service Hello bind helloEp {

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/hello"
    }
    hello(endpoint caller, http:Request req) {
        http:Response res = new;
        res.setPayload("Hello");
        caller->respond(res) but { error e => log:printError("Error when responding", err = e) };
    }
}
```
