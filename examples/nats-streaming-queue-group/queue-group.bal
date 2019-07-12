import ballerina/encoding;
import ballerina/io;
import ballerina/log;
import ballerina/nats;

// Creates a NATS connection.
nats:Connection conn = new("localhost:4222");

// Initializes the NATS Streaming listeners.
listener nats:StreamingListener lis1 = new(conn, "test-cluster", "c0");
listener nats:StreamingListener lis2 = new(conn, "test-cluster", "c1");
listener nats:StreamingListener lis3 = new(conn, "test-cluster", "c2");


// Binds the consumer to listen to the messages published to the 'demo' subject.
// Belongs to the queue group named "sample-queue-group"
@nats:StreamingSubscriptionConfig {
    subject: "demo",
    queueName: "sample-queue-group"
}
service firstQueueGroupMember on lis1 {
    resource function onMessage(nats:StreamingMessage message) {
       // Prints the incoming message in the console.
       io:println("Message Received to first queue group member: " + encoding:byteArrayToString(message.getData()));
    }

    resource function onError(nats:StreamingMessage message, nats:NatsError errorVal) {
        error e = errorVal;
        log:printError("Error occurred: ", err = e);
    }
}


// Binds the consumer to listen to the messages published to the 'demo' subject.
// Belongs to the queue group named "sample-queue-group"
@nats:StreamingSubscriptionConfig {
    subject: "demo",
    queueName: "sample-queue-group"
}
service secondQueueGroupMember on lis2 {
    resource function onMessage(nats:StreamingMessage message) {
       // Prints the incoming message in the console.
       io:println("Message Received to second queue group member: " + encoding:byteArrayToString(message.getData()));
    }

    resource function onError(nats:StreamingMessage message, nats:NatsError errorVal) {
        error e = errorVal;
        log:printError("Error occurred: ", err = e);
    }
}


// Binds the consumer to listen to the messages published to the 'demo' subject.
// Belongs to the queue group named "sample-queue-group"
@nats:StreamingSubscriptionConfig {
    subject: "demo",
    queueName: "sample-queue-group"
}
service thridQueueGroupMember on lis3 {
    resource function onMessage(nats:StreamingMessage message) {
       // Prints the incoming message in the console.
       io:println("Message Received to third queue group member: " + encoding:byteArrayToString(message.getData()));
    }

    resource function onError(nats:StreamingMessage message, nats:NatsError errorVal) {
        error e = errorVal;
        log:printError("Error occurred: ", err = e);
    }
}

