import ballerina/log;
import ballerina/rabbitmq;

// Creates a ballerina RabbitMQ connection that allows re-usability if necessary.
rabbitmq:Connection connection = new({ host: "localhost", port: 5672 });

listener rabbitmq:ChannelListener channelListener= new(connection);

// The consumer service listens to the "MyQueue" queue.
@rabbitmq:ServiceConfig {
    queueConfig: {
        queueName: "MyQueue"
    }
}
// Attaches the service to the listener.
service testSimpleConsumer on channelListener {

    // Gets triggered when a message is received by the queue.
    resource function onMessage(rabbitmq:Message message) {

        // Retrieves the text content of the message.
        var msg = message.getTextContent();
        if (msg is string) {
            log:printInfo("The message received: " + msg);
        } else {
            log:printError("Error occurred while retrieving the message content");
        }
    }
}
