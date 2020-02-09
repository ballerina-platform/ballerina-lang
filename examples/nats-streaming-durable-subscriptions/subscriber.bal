import ballerina/lang.'string as strings;
import ballerina/log;
import ballerina/nats;

// Creates a NATS connection.
nats:Connection conn = new ("localhost:4222");

// Initializes the NATS Streaming listener.
listener nats:StreamingListener lis = new (conn, clientId = "c0");

// Binds the consumer to listen to the messages published to the 'demo' subject.
@nats:StreamingSubscriptionConfig {
    subject: "demo",
    durableName: "sample-name"
}
service demoService on lis {
    resource function onMessage(nats:StreamingMessage message) {
       // Prints the incoming message in the console.
       string|error messageData = strings:fromBytes(message.getData());
       if (messageData is string) {
            log:printInfo("Message Received: " + messageData);
       } else {
            log:printError("Error occurred while obtaining message data");
       }
    }

    resource function onError(nats:StreamingMessage message, nats:Error errorVal) {
        error e = errorVal;
        log:printError("Error occurred: ", err = e);
    }
}
