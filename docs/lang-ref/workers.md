# Workers

A worker is a thread of execution. It is represented on a sequence diagram as a vertical "lifeline" of logic to be executed. 

![alt text](../images/worker-lifeline.png "The default worker in a resource")

When you create a [resource](resources.md), [function](functions.md), or [action](actions.md), a default worker is created automatically that performs the logic for that entity. You can add more workers to a resource or function, giving each worker its own logic to execute. This approach allows you to program parallel threads of execution. You can also use the [fork/join](statements.md#forkjoin) statement to easily create a set of workers and have them process in parallel.

Workers do not share any state with each other. Upon invocation, the “parent” worker can pass a message to the new worker. Resource workers can also access the service's state.

## Adding a worker

To add a worker in Design View in the Composer, drag the Worker icon from the tool palette to your sequence diagram:

![alt text](../images/icons/worker.png "Worker icon")

To add a worker when working directly with the source code of your program, a worker is defined and declared as follows:

```
worker WorkerName (message m) {
    Statement;+
    [reply MessageName;]
}
```

You then define the logic that you want the worker to reply. The worker can invoke any functions that are defined in the same package. 

If the [Reply](statements.md#reply) statement is present, it must be at the end of the worker definition. The reply indicates what message will be returned to the initiator if the initiator requires a response from the worker (see "Receiving a reply from a worker" below). If no Reply statement is present, it has the same effect as having sent a null message.

## Invoking the worker

Workers initially come into existence when the enclosing entity-- a [resource](resources.md), [function](functions.md), or [action](actions.md)--becomes active. However, similar to a resource, the worker does not execute until it has been sent a message.

A worker is triggered when a message is sent to the worker. You can configure the enclosing entity to send a message to the worker by dragging the Worker Invoke icon to the enclosing entity and typing the message name:

![alt text](../images/icons/worker-invoke.png "Worker Invoke icon")

Or type the following code in the Source View:

```
<MessageName> -> <WorkerName>;
```

For example, if the message is named `msg` and the worker is named `sampleWorker`, you would type:

```
msg -> sampleWorker;
```

## Receiving a reply from a worker

The worker can reply to the enclosing entity using a [Reply](statements.md#reply) statement at the end of the worker defintion. You can configure the enclosing entity to receive the reply by dragging the Worker Receive icon to the point where you want the enclosing entity to receive the reply:

![alt text](../images/icons/worker-receive.png "Worker Receive icon")

Or type the following code in the Source View:

```
<MessageName> <- <WorkerName>;
```

For example, to receive a reply from `sampleWorker` as a message named `msg`:
```
msg <- sampleWorker;
```

If the worker does not reply, the enclosing entity will wait until the named worker completes and will return a null value for the message.

If there is no wait for completion of a particular worker, and if the enclosing entity finishes execution, all active workers are immediately killed prior to returning from the enclosing context. 

## Examples

These examples show how to define, invoke, and receive a reply from a worker. 

### Inside the main() function

```
import ballerina.lang.system;
import ballerina.lang.message;
// Global constants are visible to worker
const int index = 12;

function main(string[] args) {
  worker sampleWorker (message m)  {
    json j;
    j = `{"name":"chanaka"}`;
    message:setJsonPayload(m, j);
    system:println("constant value is " + index);
    reply m;
  }

  message result;
  message msg = {};
  msg -> sampleWorker;
  system:println("After worker");
  result <- sampleWorker;
  string s = message:getStringPayload(result);
  system:println(s);

}
```

When you run the above Ballerina program, the following output is printed to the console:

```
After worker
constant value is 12
{"name":"chanaka"}
```

### Inside a resource

```
import ballerina.lang.message;
import ballerina.lang.system;

// Global constants are visible to worker
const int index = 12;

@BasePath ("/passthrough")
service passthrough {

    @POST
    resource passthrough (message m) {
      worker sampleWorker (message msg)  {
	json j;
	j = `{"name":"chanaka"}`;
	message:setJsonPayload(msg, j);
	system:println("constant value is " + index);
	reply msg;
      }
	message result;
	m -> sampleWorker;
	system:println("After worker");
	result <- sampleWorker;
	string s = message:getStringPayload(result);
	system:println(s);
      	reply result;
    }
}
```

When the above service is running and is sent a message, it will return the following response from the worker back to the client:

```
{
  "name": "chanaka"
}
```

### Calling a function from the worker

This example shows how a worker can call a function that's defined in the same package:

```
import ballerina.lang.system;
import ballerina.lang.message;
// Global constants are visible to worker
const int index = 12;

function main(string[] args) {
  worker sampleWorker (message m)  {
    json j;
    j = `{"name":"chanaka"}`;
    message:setJsonPayload(m, j);
    system:println("constant value is " + index);
    printHello("I'm worker");
    reply m;
  }

  message result;
  message msg = {};
  msg -> sampleWorker;
  system:println("After worker");
  printHello("I'm main function");
  result <- sampleWorker;
  string s = message:getStringPayload(result);
  system:println(s);

}

function printHello(string str) {
  system:println(str);
}
```

When you run this program, the following output is printed to the console:

```
After worker
I'm main function
constant value is 12
I'm worker
{"name":"chanaka"}
```
