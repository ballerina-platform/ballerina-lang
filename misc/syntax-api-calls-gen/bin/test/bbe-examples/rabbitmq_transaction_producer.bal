import ballerina/io;
import ballerinax/rabbitmq;

public function main() {
    // Creates a ballerina RabbitMQ channel.
    rabbitmq:Channel newChannel = new ({host: "localhost", port: 5672});

    // Declares the queue.
    var queueResult = newChannel->queueDeclare({queueName: "MyQueue"});
    if (queueResult is error) {
        io:println("An error occurred while creating the queue");
    } else {
        io:println("The queue was created successfully");
    }
    transaction {
        // Publishes the message using the routing key named "MyQueue".
        var sendResult = newChannel->basicPublish("Hello from Ballerina",
                                                  "MyQueue");
        if (sendResult is error) {
            io:println("An error occurred while sending the message");
        } else {
            io:println("The message was sent successfully");
        }
        var result = commit;
    }
}
