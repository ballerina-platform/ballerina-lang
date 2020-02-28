import ballerina/log;
import ballerina/rabbitmq;

// Creates a ballerina RabbitMQ connection that allows reusability if necessary.
rabbitmq:Connection connection = new ({host: "localhost", port: 5672});

listener rabbitmq:Listener channelListener = new (connection);

// The consumer service listens to the "MyQueue" queue.
// Quality of service settings(prefetchCount and prefetchSize) can be
// set at the listener initialization globally or per consumer service.
// These settings impose limits on the amount of data the server
// will deliver to consumers before requiring acknowledgements.
// Thus they provide a means of consumer-initiated flow control.
@rabbitmq:ServiceConfig {
    queueConfig: {
        queueName: "MyQueue"
    },
    ackMode: rabbitmq:CLIENT_ACK,
    prefetchCount: 10
}
// Attaches the service to the listener.
service QosConsumer on channelListener {

    // Gets triggered when a message is received by the queue.
    resource function onMessage(rabbitmq:Message message) {
        var messageContent = message.getTextContent();
        if (messageContent is string) {
            log:printInfo("The message received: " + messageContent);
        } else {
            log:printError("Error occurred while retrieving the message content.");
        }
        // The consumer will continue to receive messages from the server
        // once a total of 10(prefetchCount) messages are being acknowledged.
        var result = message->basicAck();
        if (result is error) {
            log:printError("Error occurred while acknowledging the message.");
        }
    }
}
