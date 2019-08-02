## Module overview

[RabbitMQ](https://www.rabbitmq.com/) is one of the most popular open-source enterprise messaging systems, modeled on the Advanced Message Queuing Protocol (AMQP) standard. This guide covers Ballerina RabbitMQ module and its public API. This module contains functionality to support messaging with RabbitMQ broker (AMQP 0-9-1).

## Samples

### RabbitMQ Producer

Following program will produce a message to a RabbitMQ server

```ballerina
import ballerina/io;
import ballerina/rabbitmq;

public function main() {
    // Creates a ballerina RabbitMQ connection that allows re-usability if necessary.
    rabbitmq:Connection connection = new({ host: "localhost", port: 5672 });

    // Creates multiple ballerina RabbitMQ channels.
    rabbitmq:Channel newChannel1 = new(connection);
    rabbitmq:Channel newChannel2 = new(connection);

    // Declares the queue, MyQueue1.
    var queueResult1 = newChannel1->queueDeclare({ queueName: "MyQueue1" });
    if (queueResult1 is rabbitmq:Error) {
        io:println("An error occurred while creating the MyQueue1 queue.");
    }

    // Declares the queue, MyQueue2.
    var queueResult2 = newChannel2->queueDeclare({ queueName: "MyQueue2" });
    if (queueResult2 is rabbitmq:Error) {
        io:println("An error occurred while creating the MyQueue2 queue.");
    }

    // Publishing messages to an exchange using a routing key.
    // Publishes the message using newChannel1 and the routing key named MyQueue1.
    worker w1 {
        var sendResult = newChannel1->basicPublish("Hello from Ballerina", "MyQueue1");
        if (sendResult is rabbitmq:Error) {
            io:println("An error occurred while sending the message to
                     MyQueue1 using newChannel1.");
        }
    }

    // Publishing messages to the same routing key using a different channel.
    // Publishes the message using newChannel2 and the same routing key named MyQueue1.
    worker w2 {
        var sendResult = newChannel2->basicPublish("Hello from Ballerina", "MyQueue1");
        if (sendResult is rabbitmq:Error) {
            io:println("An error occurred while sending the message to
                    MyQueue1 using newChannel2.");
        }
    }

    // Publishing messages to different routing keys using the same channel.
    // Publishes the message using newChannel1 to a different routing key named MyQueue2.
    worker w3 {
        var sendResult = newChannel1->basicPublish("Hello from Ballerina", "MyQueue2");
        if (sendResult is rabbitmq:Error) {
            io:println("An error occurred while sending the message to
                    MyQueue2 using newChannel1.");
        }
    }
    _ = wait {w1, w2, w3};
}
```
### RabbitMQ Subscriber

Following program will consume a message from a RabbitMQ server

```ballerina
import ballerina/log;
import ballerina/rabbitmq;

// Creates a ballerina RabbitMQ connection that allows re-usability if necessary.
rabbitmq:Connection connection = new({ host: "localhost", port: 5672 });

listener rabbitmq:Listener channelListener= new(connection);

// The consumer service listens to the "MyQueue" queue.
// The `ackMode` is by default rabbitmq:AUTO_ACK where messages are acknowledged
// immediately after consuming.
@rabbitmq:ServiceConfig {
    queueConfig: {
        queueName: "MyQueue"
    }
}
// Attaches the service to the listener.
service rabbitmqConsumer on channelListener {

    // Gets triggered when a message is received by the queue.
    resource function onMessage(rabbitmq:Message message) {

        // Retrieves the text content of the message.
        var messageContent = message.getTextContent();
        if (messageContent is string) {
            log:printInfo("The message received: " + messageContent);
        } else {
            log:printError("Error occurred while retrieving the message content.");
        }
    }
}
```