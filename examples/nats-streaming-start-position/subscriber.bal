import ballerina/encoding;
import ballerina/io;
import ballerina/log;
import ballerina/nats;

// Creates a NATS connection.
nats:Connection conn = new("localhost:4222");

// Initializes the NATS Streaming listener.
listener nats:StreamingListener lis1 = new(conn, "test-cluster", "c1");
listener nats:StreamingListener lis2 = new(conn, "test-cluster", "c2");
listener nats:StreamingListener lis3 = new(conn, "test-cluster", "c3");
listener nats:StreamingListener lis4 = new(conn, "test-cluster", "c4");
listener nats:StreamingListener lis5 = new(conn, "test-cluster", "c5");


// Binds the consumer to listen to the messages published to the 'demo' subject.
// By default receives only new messages.
@nats:StreamingSubscriptionConfig {
    subject: "demo"
}
service receiveNewOnly on lis1 {
    resource function onMessage(nats:StreamingMessage message) {
       // Prints the incoming message in the console.
       io:println("Message Received to service receiveNewOnly: "
        + encoding:byteArrayToString(message.getData()));
    }

    resource function onError(nats:StreamingMessage message, nats:NatsError errorVal) {
        error e = errorVal;
        log:printError("Error occurred: ", err = e);
    }
}

// Binds the consumer to listen to the messages published to the 'demo' subject.
// Receives all messages from the beginning.
@nats:StreamingSubscriptionConfig {
    subject: "demo",
    startPosition : nats:FIRST
}
service receiveFromBegining on lis2 {
    resource function onMessage(nats:StreamingMessage message) {
       // Prints the incoming message in the console.
       io:println("Message Received to service receiveFromBegining: "
        + encoding:byteArrayToString(message.getData()));
    }

    resource function onError(nats:StreamingMessage message, nats:NatsError errorVal) {
        error e = errorVal;
        log:printError("Error occurred: ", err = e);
    }
}

// Binds the consumer to listen to the messages published to the 'demo' subject.
// Receives messages starting from the last received message.
@nats:StreamingSubscriptionConfig {
    subject: "demo",
    startPosition : nats:LAST_RECEIVED
}
service receiveFromLastReceived on lis3 {
    resource function onMessage(nats:StreamingMessage message) {
       // Prints the incoming message in the console.
       io:println("Message Received to service receiveFromLastReceived: "
        + encoding:byteArrayToString(message.getData()));
    }

    resource function onError(nats:StreamingMessage message, nats:NatsError errorVal) {
        error e = errorVal;
        log:printError("Error occurred: ", err = e);
    }
}


// Binds the consumer to listen to the messages published to the 'demo' subject.
// Receives messages starting from the provided sequence number.
@nats:StreamingSubscriptionConfig {
    subject: "demo",
    startPosition : 3
}
service receiveFromGivenIndex on lis4 {
    resource function onMessage(nats:StreamingMessage message) {
       // Prints the incoming message in the console.
       io:println("Message Received to service receiveFromGivenIndex: "
        + encoding:byteArrayToString(message.getData()));
    }

    resource function onError(nats:StreamingMessage message, nats:NatsError errorVal) {
        error e = errorVal;
        log:printError("Error occurred: ", err = e);
    }
}


// Binds the consumer to listen to the messages published to the 'demo' subject.
// Recieves messages since the provided historical time delta.
@nats:StreamingSubscriptionConfig {
    subject: "demo",
    startPosition : { timeDelta: 5, timeUnit: nats:SECONDS }
}
service receiveSinceTimeDelta on lis5 {
    resource function onMessage(nats:StreamingMessage message) {
       // Prints the incoming message in the console.
       io:println("Message Received to service receiveSinceTimeDelta: "
        + encoding:byteArrayToString(message.getData()));
    }

    resource function onError(nats:StreamingMessage message, nats:NatsError errorVal) {
        error e = errorVal;
        log:printError("Error occurred: ", err = e);
    }
}
