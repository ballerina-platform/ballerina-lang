import ballerina/log;
import ballerina/nats;

// Initializes the NATS listener.
listener nats:Listener subscription = new({ host: "localhost",
                                            port: 4222,
                                            clientId: "s0" });

// Binds the consumer to listen to the messages published to the 'demo' subject.
@nats:ConsumerConfig {
    subject: "demo"
}
service demo on subscription {

    resource function onMessage(nats:Message msg) {
        // Prints the incoming message in the console.
        log:printInfo("Received message : " + msg.getData());
    }

}
