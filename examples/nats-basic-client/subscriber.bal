import ballerina/log;
import ballerina/nats;

// Initializes a connection.
nats:Connection connection = new(url = "nats://localhost:4222");

// Initializes the NATS listener.
listener nats:Listener subscription = new(connection);

// Binds the consumer to listen to the messages published to the 'demo' subject.
@nats:SubscriptionConfig {
    subject: "demo"
}
service demo on subscription {

    resource function onMessage(nats:Message msg, string data) {
        // Prints the incoming message in the console.
        log:printInfo("Received message : " + data);
    }

    resource function onError(nats:Message msg, nats:Error err) {
        log:printError("Error occurred in data binding", err);
    }
}
