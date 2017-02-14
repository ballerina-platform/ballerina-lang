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
