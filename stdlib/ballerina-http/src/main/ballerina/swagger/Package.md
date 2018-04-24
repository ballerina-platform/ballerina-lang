## Package overview
This package provides the code generation capabilities to ballerina use cases. Following types of code generation can be performed with this package.
1. Generate Ballerina code for a provided swagger definition.
2. Generate client stub for existing Ballerina service at build time.
3. Export the swagger definition of a Ballerina service.

Ballerina swagger command is used for Swagger to Ballerina and Ballerina to Swagger code generation.
Ballerina to Swagger can generate two types of sources,
* Mock service
* Client stub

For build time client stub generation, annotation support is provided.

### Mock Service from Swagger
`ballerina swagger mock <swaggerfile> 
    [-p packagename>|--package <packagename>] 
    [-o <path>|--output<path>]`

Generates a ballerina service for the swagger file.

This generated service can be used as a mock version of the actual service implementation. Generated sources will contain service definition in gen/ and resource implementation file in package root directory with suffix _impl. _impl file is not overwritten by code regeneration.

### Client Stub from Swagger
`ballerina swagger client <swaggerfile> 
    [-p packagename>|--package <packagename>] 
    [-o <path>|--output<path>]`
    
Generates a ballerina client stub for the service defined in swagger file. 

This client can be used in client applications to call the service defined in swagger file.

### Service to Swagger Export
`ballerina swagger export <balfile> 
    [-o <path>|--output<path>]
    [-s <servicename>|--service <servicename>]`

Export the ballerina service to a OpenApi Specification 3.0 definition. 
For export to work properly input ballerina service must contain basic service and resource level http annotations defining the service.

### Client Stub for Service
Generates a Ballerina client stub to communicate with a Ballerina service.

`@swagger:ClientEndpoint` annotation is used to mark endpoint(s) to be used for client generation. Endpoints which are not marked with this annotation will not be picked up for client code generation. If required endpoints are not marked with this annotation it may cause errors during client stub generation.
`@swagger:ClientConfig {generate: true}` annotation is used to enable client stub generation. This is a service level annotation and should be used for enable disable client code generation per service.

## Samples
### Generating mock service from swagger
`ballerina swagger mock hello_service.yaml -p hello_service`

### Generating client stub from swagger
`ballerina swagger client hello_service.yaml -p hello_client`

### Export swagger of a service
`ballerina swagger export hello_service.bal`

### Generating Client stub for service
```ballerina
import ballerina/io;
import ballerina/http;
import ballerina/swagger;
import ballerina/log;

// Defines this endpoint as a selected endpoint for client generation
@swagger:ClientEndpoint
endpoint http:Listener helloEp {
    port: 9090
};

// Enable the client code generation for this service
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

## Package contents