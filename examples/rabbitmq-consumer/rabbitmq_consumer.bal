import ballerina/log;
import ballerina/rabbitmq;

// Creates a ballerina RabbitMQ connection that allows re-usability if necessary.
rabbitmq:Connection connection = new ({host: "localhost", port: 5672});

listener rabbitmq:Listener channelListener = new (connection);

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
