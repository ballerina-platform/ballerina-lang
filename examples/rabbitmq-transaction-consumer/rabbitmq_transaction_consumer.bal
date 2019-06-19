import ballerina/log;
import ballerina/rabbitmq;

// The consumer service listens to the "MyQueue" queue.
@rabbitmq:ServiceConfig {
    queueConfig: {
        queueName: "MyQueue"
    },
    ackMode: rabbitmq:CLIENT_ACK
}
// Attaches the service to the listener.
service testSimpleConsumer on new rabbitmq:ChannelListener({ host: "localhost", port: 5672 }) {

    // Gets triggered when a message is received by the queue.
    resource function onMessage(rabbitmq:Message message) {
        transaction {
            var msg = message.getTextContent();
            if (msg is string) {
                log:printInfo("The message received: " + msg);
            } else {
                log:printError("Error occurred while retrieving the message content");
            }
            // Positively acknowledges a single message.
            var result = message->basicAck();
            if (result is error) {
                log:printError("Error occurred while acknowledging the message");
            }
        }
    }
}

