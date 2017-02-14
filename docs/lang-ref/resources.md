# Resources

A resource is a single request handler within a [service](services.md). When you create a service in Ballerina using the visual editor, a default resource is automatically created as well. The resource contains the integration logic.

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

m1 and m2 are messages that are passed by a client as input to the resource named resource-1 and resource-2, respectively. resource-1 will produce the message response1 as a result, and resource-2 will produce response2. To compute the response message, resource-1 relays message m1 to connector Connector-1 and will receive response1; similarly, resource-2 relays message m2 to connector Connector-2 and will receive response2.
