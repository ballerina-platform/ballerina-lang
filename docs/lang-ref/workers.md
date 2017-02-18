# Workers

A worker is a thread that executes a [function](functions.md).

A worker is defined and declared as follows:
```
worker WorkerName (message m) {
    VariableDeclaration;*
    Statement;+
    [reply MessageName;]
}
```

## Initiating the worker

Workers initially come into existence when the enclosing entity-- a [resource](resources.md), [function](functions.md), or [action](actions.md)--becomes active. However, similar to a resource, the worker does not execute until it has been sent a message.

A worker is triggered when a message is sent to the worker as follows by the enclosing entity:
```
MessageName -> WorkerName;
Waiting for Worker Completion
```
When the worker replies, the response message (if any) is received by the enclosing entity from the worker as follows:
```
MessageName <- WorkerName;
Replying from a Worker
```
The worker can reply to the enclosing entity using a Reply statement.
