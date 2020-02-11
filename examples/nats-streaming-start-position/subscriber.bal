import ballerina/lang.'string as strings;
import ballerina/log;
import ballerina/nats;

// Creates a NATS connection.
nats:Connection conn = new ("localhost:4222");

// Initializes the NATS Streaming listener.
listener nats:StreamingListener lis = new (conn);

// Binds the consumer to listen to the messages published to the 'demo' subject.
// By default, only new messages are received.
@nats:StreamingSubscriptionConfig {
    subject: "demo"
}
service receiveNewOnly on lis {
    resource function onMessage(nats:StreamingMessage message) {
        // Prints the incoming message in the console.
        string|error messageData = strings:fromBytes(message.getData());
        if (messageData is string) {
            log:printInfo("Message Received to service receiveNewOnly: "
                                                                  + messageData);
        } else {
            log:printError("Error occurred while obtaining message data");
        }
    }

    resource function onError(nats:StreamingMessage message, nats:Error errorVal) {
        error e = errorVal;
        log:printError("Error occurred: ", e);
    }
}

// Binds the consumer to listen to the messages published to the 'demo' subject.
// Receives all messages from the beginning.
@nats:StreamingSubscriptionConfig {
    subject: "demo",
    startPosition: nats:FIRST
}
service receiveFromBegining on lis {
    resource function onMessage(nats:StreamingMessage message) {
        // Prints the incoming message in the console.
        string|error messageData = strings:fromBytes(message.getData());
        if (messageData is string) {
            log:printInfo("Message Received to service receiveFromBegining: "
                                                                  + messageData);
        } else {
            log:printError("Error occurred while obtaining message data");
        }
    }

    resource function onError(nats:StreamingMessage message, nats:Error errorVal) {
        error e = errorVal;
        log:printError("Error occurred: ", e);
    }
}

// Binds the consumer to listen to the messages published to the 'demo' subject.
// Receives messages starting from the last received message.
@nats:StreamingSubscriptionConfig {
    subject: "demo",
    startPosition: nats:LAST_RECEIVED
}
service receiveFromLastReceived on lis {
    resource function onMessage(nats:StreamingMessage message) {
        // Prints the incoming message in the console.
        string|error messageData = strings:fromBytes(message.getData());
        if (messageData is string) {
            log:printInfo("Message Received to service receiveFromLastReceived: "
                                                                  + messageData);
        } else {
            log:printError("Error occurred while obtaining message data");
        }
    }

    resource function onError(nats:StreamingMessage message, nats:Error errorVal) {
        error e = errorVal;
        log:printError("Error occurred: ", e);
    }
}

[nats:SEQUENCE_NUMBER, int] sequenceNo = [nats:SEQUENCE_NUMBER, 3];
// Binds the consumer to listen to the messages published to the 'demo' subject.
// Receives messages starting from the provided sequence number.
@nats:StreamingSubscriptionConfig {
    subject: "demo",
    startPosition: sequenceNo
}
service receiveFromGivenIndex on lis {
    resource function onMessage(nats:StreamingMessage message) {
        // Prints the incoming message in the console.
        string|error messageData = strings:fromBytes(message.getData());
        if (messageData is string) {
            log:printInfo("Message Received to service receiveFromGivenIndex: "
                                                                  + messageData);
        } else {
            log:printError("Error occurred while obtaining message data");
        }
    }

    resource function onError(nats:StreamingMessage message, nats:Error errorVal) {
        error e = errorVal;
        log:printError("Error occurred: ", e);
    }
}

[nats:TIME_DELTA_START, int] timeDelta = [nats:TIME_DELTA_START, 5];
// Binds the consumer to listen to the messages published to the 'demo' subject.
// Receives messages since the provided historical time delta.
@nats:StreamingSubscriptionConfig {
    subject: "demo",
    startPosition: timeDelta
}
service receiveSinceTimeDelta on lis {
    resource function onMessage(nats:StreamingMessage message) {
        // Prints the incoming message in the console.
        string|error messageData = strings:fromBytes(message.getData());
        if (messageData is string) {
            log:printInfo("Message Received to service receiveSinceTimeDelta: "
                                                                  + messageData);
        } else {
            log:printError("Error occurred while obtaining message data");
        }
    }

    resource function onError(nats:StreamingMessage message, nats:Error errorVal) {
        error e = errorVal;
        log:printError("Error occurred: ", e);
    }
}
