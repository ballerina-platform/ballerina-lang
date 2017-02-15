# Workers

Workers represent parallel threads of execution in Ballerina. There are two kinds of workers: implicit workers and explicit workers. Implicit workers are those that are created externally to the executable entity in question (resource, function or action) and simply represent the caller's thread being used to execute the callee's statements.

Explicit workers are those that are defined and programmed explicitly by placing any number of executable statements in the worker. Graphically, workers are represented by sequence diagram lifelines.

# Defining & Declaring Workers

A `worker` is defined and declared as follows:
```
worker WorkerName (message m) {
    Statement;+
    [reply MessageName;]
}
```

If the `reply` statement is present it must be at the end of the worker. The `reply` indicates what message will be returned to the initiator if the initiator chooses to receive a response from the worker. If no `reply` statement is present it has the same effect of having sent a null message.

# Initiating Workers

Workers initially come into existence when the enclosing entity (resource, function, or action)
becomes active. However, similar to a resource, the worker does not execute until it
has been sent a message.

A worker is triggered when a message is sent to the worker as follows by the enclosing entity:

```
MessageName -> WorkerName;
```

# Waiting for Worker Completion

When the worker replies, the response message (if any) is received by the enclosing entity
from the worker as follows:
```
MessageName <- WorkerName;
```

If the worker does not reply, then the above will wait until the named worker completes and return a null value for the message.

If there is no wait for completion of a particular worker and if the enclosing entity finishes execution then all active workers are immediately killed prior to returning from the enclosing context.

> NOTE: In version 0.8 only one interaction is allowed between the worker and its creator.
