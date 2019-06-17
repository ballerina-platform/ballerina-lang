import ballerina/log;
import ballerina/rabbitmq;

// Creates a ballerina RabbitMQ connection that allows reusability if necessary.
rabbitmq:Connection connection = new({ host: "localhost", port: 5672 });

listener rabbitmq:ChannelListener channelListener = new(connection);

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
service testClientAcknowledgement on channelListener {

    // Gets triggered when a message is received by the queue.
    resource function onMessage(rabbitmq:Message message) {
        // Retrieving the text content of the message.
        var msg = message.getTextContent();
        if (msg is string) {
            log:printInfo("The message received: " + msg);
        } else {
            log:printError("Error occurred while retrieving the message content.");
        }
        // Positively acknowledges a single message.
        var result = message->basicAck();
        if (result is error) {
            log:printError("Error occurred while acknowledging the message");
        }
    }
}
