import ballerina/log;
import ballerina/rabbitmq;

// Creates a ballerina RabbitMQ connection that allows reusability if necessary.
rabbitmq:Connection conn = new({ host: "localhost", port: 5672 });

public function main() {
    // Creates a ballerina RabbitMQ channel.
    rabbitmq:Channel chann = new(conn);

    // Publishes the message using the routing key named "testing".
    var sendResult = chann->basicPublish("Hello from Ballerina", "testing");
    if (sendResult is error) {
        log:printError("An error occurred while sending the message");
    } else {
        log:printInfo("The message was sent successfully");
    }
}
