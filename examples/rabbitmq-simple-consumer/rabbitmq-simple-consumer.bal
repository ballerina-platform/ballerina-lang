import ballerina/log;
import ballerina/rabbitmq;

// Creates a ballerina RabbitMQ connection that allows reusability if necessary.
rabbitmq:Connection conn = new({ host: "localhost", port: 5672 });

listener rabbitmq:ChannelListener chann = new(conn);

// Consumer service listens to the "testing" queue.
@rabbitmq:ServiceConfig {
    queueName: "testing"
}
// Attaches the service to the listener.
service testSimpleConsumer on chann {

    // Gets triggered when a message is received by the queue.
    resource function onMessage(string message) {
        log:printInfo("The message received: " + message);
    }
}
