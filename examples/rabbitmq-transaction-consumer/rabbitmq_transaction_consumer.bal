import ballerina/log;
import ballerina/rabbitmq;

// The consumer service listens to the "MyQueue" queue.
@rabbitmq:ServiceConfig {
    queueConfig: {
        queueName: "MyQueue"
    }
}
// Attaches the service to the listener.
service transactionConsumer on new rabbitmq:ChannelListener({ host: "localhost", port: 5672 }) {

    // Gets triggered when a message is received by the queue.
    resource function onMessage(rabbitmq:Message message) {
        transaction {
            var messageContent = message.getTextContent();
            if (messageContent is string) {
                log:printInfo("The message received: " + messageContent);
            } else {
                log:printError("Error occurred while retrieving the message content.");
            }
            // Positively acknowledges a single message.
            // The acknowledgement gets committed upon successful execution of the transaction,
            // or will rollback otherwise.
            var result = message->basicAck();
            if (result is error) {
                log:printError("Error occurred while acknowledging the message.");
            }
        }
    }
}
