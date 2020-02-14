import ballerina/lang.'string as strings;
import ballerina/log;
import ballerina/nats;

// Creates a NATS connection.
nats:Connection conn = new ("localhost:4222");

// Initializes the NATS Streaming listeners.
listener nats:StreamingListener lis = new (conn);


// Binds the consumer to listen to the messages published to the 'demo' subject.
// Belongs to the queue group named "sample-queue-group"
@nats:StreamingSubscriptionConfig {
    subject: "demo",
    queueName: "sample-queue-group"
}
service firstQueueGroupMember on lis {
    resource function onMessage(nats:StreamingMessage message) {
        // Prints the incoming message in the console.
        string|error messageData = strings:fromBytes(message.getData());
        if (messageData is string) {
            log:printInfo("Message Received to first queue group member: "
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
// Belongs to the queue group named "sample-queue-group"
@nats:StreamingSubscriptionConfig {
    subject: "demo",
    queueName: "sample-queue-group"
}
service secondQueueGroupMember on lis {
    resource function onMessage(nats:StreamingMessage message) {
        // Prints the incoming message in the console.
        string|error messageData = strings:fromBytes(message.getData());
        if (messageData is string) {
            log:printInfo("Message Received to second queue group member: "
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
// Belongs to the queue group named "sample-queue-group"
@nats:StreamingSubscriptionConfig {
    subject: "demo",
    queueName: "sample-queue-group"
}
service thridQueueGroupMember on lis {
    resource function onMessage(nats:StreamingMessage message) {
        // Prints the incoming message in the console.
        string|error messageData = strings:fromBytes(message.getData());
        if (messageData is string) {
            log:printInfo("Message Received to third queue group member: "
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

