## Module overview

'ballerina/rabbitmq' provides the capability to connect with a RabbitMQ server and perform the following

- Point to point communication (Queues)
- Pub/Sub (Topics)

## Samples

### RabbitMQ Producer

Following program will produce a message to a RabbitMQ server

```ballerina
import ballerina/rabbitmq;
import ballerina/log;

public function main() {
     rabbitmq:Channel chann = new({ host: "localhost", port: 5672 });
     var sendResult = chann->basicPublish("Hello from ballerina", "testingDemo", exchange = "");
     if (sendResult is error) {
          log:printError("An error occurred while sending the message");
     } else {
          log:printInfo("The message was sent successfully");
     }
}
```
### RabbitMQ Subscriber

Following program will consume a message from a RabbitMQ server

```ballerina
import ballerina/rabbitmq;
import ballerina/log;

listener rabbitmq:ChannelListener chann = new({ host: "localhost", port: 5672 });

@rabbitmq:ServiceConfig {
        queueName: "testingDemo"
}
service testSimpleConsumer on chann {
    resource function onMessage(string message) {
            log:printInfo("The message received: " + message);
    }
}
```