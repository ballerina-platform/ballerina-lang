import ballerina/log;
import ballerina/nats;

// Creates a NATS connection.
nats:Connection conn = new ("localhost:4222");

// Initializes the NATS Streaming listener.
listener nats:StreamingListener lis = new (conn);

// Binds the consumer to listen to the messages published to the 'demo' subject.
@nats:StreamingSubscriptionConfig {
    subject: "demo"
}
service demoService on lis {
    resource function onMessage(nats:StreamingMessage message, json data) {
        // Converts JSON data to string.
        string | error val = data.toJsonString();
        if (val is string) {
            // Prints the incoming message in the console.
            log:printInfo("Message Received: " + val);
        } else {
            log:printError("Error occurred during json to string conversion",
                                                                      err = val);
        }
    }

    resource function onError(nats:StreamingMessage message, nats:Error errorVal) {
        error e = errorVal;
        log:printError("Error occurred: ", err = e);
    }
}
