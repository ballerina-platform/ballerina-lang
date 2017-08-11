# Resources

A resource is a construct that handles one request within a [service](services.md). 

## Defining a resource
When you create a service in Ballerina using the Design view in the Composer, a default resource is automatically created within that service so that you can add your integration logic to it. To add another resource, drag the Resource icon ![alt-text](../images/icons/resource.png "Resource icon") to the canvas. 

A resource is defined as follows:

```
[ResourceAnnotations]
resource ResourceName (Message VariableName[, ([ResourceParamAnnotations] TypeName VariableName)+]) {
    ConnectorDeclaration;*
    VariableDeclaration;*
    WorkerDeclaration;*
    Statement;+
}*
```

The visual representation of this (without the annotations) in a sequence diagram is as follows:

![alt text](../images/bal-resource-skeleton.png "Sequence diagram showing resources receiving requests and sending replies")

In this example, m1 and m2 are messages that are passed by a client as input to the resources named resource-1 and resource-2, respectively. As a result, resource-1 will produce the message response1, and resource-2 will produce response2. To compute the response message, resource-1 relays message m1 to connector Connector-1 and will receive response1; similarly, resource-2 relays message m2 to connector Connector-2 and will receive response2.

A resource can have state, which lasts as long as the resource is active. It can also be a [worker](workers.md).
