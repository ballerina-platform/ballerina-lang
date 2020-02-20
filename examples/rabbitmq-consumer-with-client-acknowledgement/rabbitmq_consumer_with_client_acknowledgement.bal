import ballerina/log;
import ballerina/rabbitmq;

// Creates a ballerina RabbitMQ connection that allows re-usability if necessary.
rabbitmq:Connection connection = new ({host: "localhost", port: 5672});

listener rabbitmq:Listener channelListener = new (connection);

// The consumer service listens to the "MyQueue" queue.
// ackMode is by default rabbitmq:AUTO_ACK which will automatically acknowledge
// all messages once consumed.
@rabbitmq:ServiceConfig {
    queueConfig: {
        queueName: "MyQueue"
    },
    ackMode: rabbitmq:CLIENT_ACK
}
// Attaches the service to the listener.
service rabbitmqConsumerAck on channelListener {

    // Gets triggered when a message is received by the queue.
    resource function onMessage(rabbitmq:Message message) {

        // Retrieves the text content of the message.
        var messageContent = message.getTextContent();
        if (messageContent is string) {
            log:printInfo("The message received: " + messageContent);
        } else {
            log:printError("Error occurred while retrieving the message content.");
        }

        // Positively acknowledges a single message.
        var result = message->basicAck();
        if (result is error) {
            log:printError("Error occurred while acknowledging the message.");
        }
    }
}
