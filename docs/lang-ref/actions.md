# Actions

An action is an operation you can execute against a [connector](connectors.md). It represents a single interaction with a participant of the integration. A connector defines a set of actions. 

The structure of an action definition is as follows:
```
[ActionAnnotations]

action ActionName (ConnectorName VariableName[, ([ActionParamAnnotations] TypeName VariableName)+]) (TypeName*)
        [throws exception] {
    ConnectorDeclaration;*
    VariableDeclaration;*
    WorkerDeclaration;*
    Statement;+
}
```
