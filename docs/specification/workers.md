# Workers

#### Defining & Declaring Workers
A `worker` is defined and declared as follows:
```
worker WorkerName (message m) {
    VariableDeclaration;*
    Statement;+
    [reply MessageName;]
}
```

#### Initiating the Worker

Workers initially come into existence when the enclosing entity (resource, function, or action)
becomes active. However, similar to a resource, the worker does not execute until it
has been sent a message.

A worker is triggered when a message is sent to the worker as follows by the enclosing entity:

```
MessageName -> WorkerName;
```

#### Waiting for Worker Completion

When the worker replies, the response message (if any) is received by the enclosing entity
from the worker as follows:
```
MessageName <- WorkerName;
```

#### Replying from a Worker

If the worker wishes to reply to the enclosing entity, it can do so using a `reply` statement.
