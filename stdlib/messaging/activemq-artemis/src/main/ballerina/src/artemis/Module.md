## Module overview

This module provides the capability to connect with a ActiveMQ Artemis broker using the [Core API](https://activemq.apache.org/artemis/docs/latest/core.html).

## Samples

### Artemis Producer

The following program produces message to the Artemis broker.

```ballerina
import ballerina/artemis;
import ballerina/log;

artemis:Producer prod = new({host:"localhost", port:61616}, "demo");
public function main() {
    var err = prod->send("Hello World!");
    if (err is error) {
        log:printError("Error occurred while sending message", err = err);
    }
    // Close the producer
    if (!prod.isClosed()) {
        err = prod->close();
        if (err is error) {
            log:printError("Error occurred closing producer", err = err);
        }
    }
}
```
### Artemis consumer

The following program will consume a message from the Artemis broker.

```ballerina
@artemis:ServiceConfig {
    queueConfig: {
        queueName: "demo"
    }
}
service artemisConsumer on new artemis:Listener({host:"localhost", port:61616}) {
    resource function onMessage(artemis:Message message) {
        var payload = message.getPayload();
        if (payload is string) {
            io:println("Recieved: " + payload);
        }
    }
}
```