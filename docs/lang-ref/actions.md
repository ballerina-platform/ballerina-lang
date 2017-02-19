# Actions

An action is an operation you can execute against a [connector](connectors.md). It represents a single interaction with a participant of the integration. A connector defines a set of actions. For example, the Twitter connector includes actions such as `tweet`, `retweet`, and `destroyStatus`.

In the Composer, you invoke an action by navigating to the connector in the tool palette and dragging the icon of the action you want to the canvas.

You can define a new action by dragging the Action icon to the connector construct on the canvas:

![alt text](../images/icons/action.png "Action icon") 

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
