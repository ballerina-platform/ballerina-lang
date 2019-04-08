import ballerina/io;
import ballerina/rabbitmq;

// Creates a ballerina RabbitMQ connection that allows reusability if necessary.
rabbitmq:Connection connection = new({ host: "localhost", port: 5672 });

public function main() {
    // Creates a ballerina RabbitMQ channel.
    rabbitmq:Channel newChannel = new(connection);

    // Declares the queue.
    var queueResult = newChannel->queueDeclare(queueConfig = { queueName: "testQueue" });
    if (queueResult is error) {
        io:println("An error occurred while creating the queue");
    } else {
        io:println("The queue was created successfully");
    }

    // Publishes the message using the routing key named "testing".
    var sendResult = newChannel->basicPublish("Hello from Ballerina", "testQueue");
    if (sendResult is error) {
        io:println("An error occurred while sending the message");
    } else {
        io:println("The message was sent successfully");
    }
}
