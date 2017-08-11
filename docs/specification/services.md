# Services & Resources

## Services

Services represent collections of network accessible entry points in Ballerina. A service is bound to particular network protocols using annotations.

A `service` is defined as follows:
```
[ServiceAnnotations]
service ServiceName {
    VariableDeclaration;*
    ResourceDefinition;+
}
```
Services are singletons. As such all variables defined within a service scope are shared across all `resource` invocations. These variables come into existence when the service is first loaded into the system and as such no assumptions can be made about exactly when that may occur.

Service annotations are used to bind the service to a network protocol. Such bindings are sometimes referred to as 'Server Connectors'. In version 0.8, the following network protocols are supported:
- HTTP/1.1
- HTTP/2.0 (partially)
- WebSockets (partially)
- JMS
- Polling via FTP/FTPS/SFTP

All of these are defined using the `@Source` annotations. Details of the attributes that are available for each protocol are given in the documentation for that server connector. NOTE: After v0.8.0 this annotation architecture will be revisited as this is a magic annotation and hence it is not clean.

By default Ballerina assumes that the service is to be exposed via HTTP/1.1 using the system default port and that all requests coming to the HTTP server will be delivered to this service.

## Resources

Resources represent a single network accessible entry point in Ballerina. Exactly how the resource is exposed over a network protocol is dependent on the server connector in use for the service as well as on annotations that are given for the specific resource. All resources are invoked with an argument of type `message`, the built-in reference type representing a network invocation. Depending on the network protocol, additional annotations may be used to map other request information and bind them to additional parameters. Each server connector will provide a set of utility functions to access information from the required `message` argument.

A `resource` is defined as follows:
```
[ResourceAnnotations]
[native] resource ResourceName (message VariableName[, ([ResourceParamAnnotations] TypeName VariableName)+]) {
    WorkerDeclaration;*
    Statement;+
}*
```

The `native` keyword says that the resource's implementation is not in Ballerina. In that case the body of the resource is not given as it is opaque.

If the enclosing service is using the default HTTP/1.1 server connector then Ballerina assumes that HTTP requests for GET to the path represented by the name of the resource will be processed by this resource.

## Example

TO BE WRITTEN. See samples for now.
