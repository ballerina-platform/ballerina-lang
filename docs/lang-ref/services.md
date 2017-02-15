# Services

When defining a Ballerina program as a service instead of an executable program, the `service` construct acts as the top-level container that holds all the integration logic and can interact with the rest of the world. Its base path is the context part of the URL that clients use when sending requests to the service. Note that you can have multiple services in a single Ballerina program, each with their own base path. This is useful in more complex scenarios like the Content-Based Routing sample.

A service is defined as follows:

```
[ServiceAnnotations]
service ServiceName {
  ConnectorDeclaration;*
  VariableDeclaration;*
  ResourceDefinition;+
}
```

A service is a container of [resources](resources.md), each of which defines the logic for handling one type of request. Services are singletons, so all variables defined within a service scope are shared across all resource invocations. A service can have state for as long as it's active. 
