import ballerina/encoding;
import ballerina/log;
import ballerina/nats;

// Creates a NATS connection.
nats:Connection conn = new("localhost:4222");

// Initializes the NATS Streaming listener.
listener nats:StreamingListener lis = new(conn);

// Binds the consumer to listen to the messages published to the 'demo' subject.
@nats:StreamingSubscriptionConfig {
    subject: "demo"
}
service demoService on lis {
    resource function onMessage(nats:StreamingMessage message) {
       // Prints the incoming message in the console.
       log:printInfo("Message Received: " + encoding:byteArrayToString(message.getData()));
    }

    resource function onError(nats:StreamingMessage message, nats:Error errorVal) {
        error e = errorVal;
        log:printError("Error occurred: ", err = e);
    }
}
