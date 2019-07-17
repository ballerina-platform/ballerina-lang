import ballerina/encoding;
import ballerina/log;
import ballerina/nats;

// Creates a NATS connection.
nats:Connection conn = new("localhost:4222");

// Initializes the NATS Streaming listener.
listener nats:StreamingListener lis = new(conn);

// Binds the consumer to listen to the messages published to the 'demo' subject.
// By default, only new messages are received.
@nats:StreamingSubscriptionConfig {
    subject: "demo"
}
service receiveNewOnly on lis {
    resource function onMessage(nats:StreamingMessage message) {
       // Prints the incoming message in the console.
       log:printInfo("Message Received to service receiveNewOnly: "
        + encoding:byteArrayToString(message.getData()));
    }

    resource function onError(nats:StreamingMessage message, nats:Error errorVal) {
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
service receiveFromBegining on lis {
    resource function onMessage(nats:StreamingMessage message) {
       // Prints the incoming message in the console.
       log:printInfo("Message Received to service receiveFromBegining: "
        + encoding:byteArrayToString(message.getData()));
    }

    resource function onError(nats:StreamingMessage message, nats:Error errorVal) {
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
service receiveFromLastReceived on lis {
    resource function onMessage(nats:StreamingMessage message) {
       // Prints the incoming message in the console.
       log:printInfo("Message Received to service receiveFromLastReceived: "
        + encoding:byteArrayToString(message.getData()));
    }

    resource function onError(nats:StreamingMessage message, nats:Error errorVal) {
        error e = errorVal;
        log:printError("Error occurred: ", err = e);
    }
}


// Binds the consumer to listen to the messages published to the 'demo' subject.
// Receives messages starting from the provided sequence number.
@nats:StreamingSubscriptionConfig {
    subject: "demo",
    startPosition : [nats:SEQUENCE_NUMBER, 3]
}
service receiveFromGivenIndex on lis {
    resource function onMessage(nats:StreamingMessage message) {
       // Prints the incoming message in the console.
       log:printInfo("Message Received to service receiveFromGivenIndex: "
        + encoding:byteArrayToString(message.getData()));
    }

    resource function onError(nats:StreamingMessage message, nats:Error errorVal) {
        error e = errorVal;
        log:printError("Error occurred: ", err = e);
    }
}


// Binds the consumer to listen to the messages published to the 'demo' subject.
// Recieves messages since the provided historical time delta.
@nats:StreamingSubscriptionConfig {
    subject: "demo",
    startPosition : [nats:TIME_DELTA_START, 5]
}
service receiveSinceTimeDelta on lis {
    resource function onMessage(nats:StreamingMessage message) {
       // Prints the incoming message in the console.
       log:printInfo("Message Received to service receiveSinceTimeDelta: "
        + encoding:byteArrayToString(message.getData()));
    }

    resource function onError(nats:StreamingMessage message, nats:Error errorVal) {
        error e = errorVal;
        log:printError("Error occurred: ", err = e);
    }
}
