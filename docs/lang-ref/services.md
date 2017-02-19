# Services

When defining a Ballerina program as a service instead of an executable program, the `service` construct acts as the top-level container that holds all the integration logic and can interact with the rest of the world. Its base path is the context part of the URL that clients use when sending requests to the service. You create one service per Ballerina program file.

A service is a container of [resources](resources.md), each of which defines the logic for handling one type of request. Services are singletons, so all variables defined within a service scope are shared across all resource invocations. A service can have state for as long as it's active. 

## Defining a service

To define a service in the Composer, drag the service icon ![alt-text](../images/icons/Service.png) from the tool palette to the canvas. You can then set the base path annotation using the Annotations button in the upper right corner of the service, and define any variables the service needs by clicking the Variables button in the upper left corner. A new resource is added automatically to the service, where you can start adding your integration logic. You can add more resources as needed.

A service is defined as follows:

```
[ServiceAnnotations]
service ServiceName {
  ConnectorDeclaration;*
  VariableDeclaration;*
  ResourceDefinition;+
}
```

