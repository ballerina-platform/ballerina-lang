## Module overview

'ballerina/nats' provides the capability to connect with a NATS server and perform the following

- Point to point communication (Queues)
- Pub/Sub (Topics)
- Request/Reply

## Samples

### NATS Producer

Following program will produce a message to a NATS server

```ballerina
import ballerina/nats;
import ballerina/io;

public function main() {
    nats:Producer producer = new({ host: "localhost", port: 4222, clientId: "p0" });
    var result = producer->send("demo", "Hello Ballerina !!");
    if (result is error) {
       io:println("Error occurred while producing the message.");
    } else {
       io:println("GUID "+result+" received for the produced message.");
    }
}
```
### NATS Subscriber

Following program will consume a message from a NATS server

```ballerina
import ballerina/nats;
import ballerina/io;

listener nats:Listener subscription = new({ host: "localhost", port: 4222, clientId: "s0" });

@nats:ConsumerConfig { subject: "demo" }
service demo on subscription {
    resource function onMessage(nats:Message msg) {
        io:println("Recived message : " + msg.getData());
    }
}
```