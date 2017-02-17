# Actions

An action is an operation you can execute against a [connector](connectors.md). It represents a single interaction with a participant of the integration. A connector defines a set of actions. For example, the Twitter connector includes actions such as `tweet`, `retweet`, and `destroyStatus`.

In the Composer, you invoke an action by dragging the Action Invoke icon ![alt text](../images/icons/action-invoke.png "Action Invoke icon") to the canvas. 

You can define a new action by dragging the Action icon ![alt text](../images/icons/action.png "Action icon") to the connector construct on the canvas. 

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
