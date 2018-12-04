# Remote Function

A remote function is an operation you can execute against a [connector](connectors.md). It represents a single interaction with a participant of the integration. A connector defines a set of remote functions. For example, the Twitter connector includes remote functions such as `tweet`, `retweet`, and `destroyStatus`.

In the Composer, you invoke a remote function by navigating to the connector in the tool palette and dragging the icon of the remote function you want to the canvas.

You can define a new remote function by dragging the Remote Function icon to the connector construct on the canvas:

![alt text](../images/icons/action.png "Remote function icon") 

The structure of a remote function definition is as follows:
```
[ActionAnnotations]

remote function remoteFuncitonName (ConnectorName VariableName[, ([ActionParamAnnotations] TypeName VariableName)+]) (TypeName*)
        [throws exception] {
    ConnectorDeclaration;*
    VariableDeclaration;*
    WorkerDeclaration;*
    Statement;+
}
```
