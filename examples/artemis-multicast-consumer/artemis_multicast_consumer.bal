import ballerina/artemis;
import ballerina/log;
import ballerina/io;

// Consumer listens to the queue (i.e., "my_queue") with the address
// (i.e., "my_address").
@artemis:ServiceConfig {
    queueConfig: {
        queueName: "my_queue",
        addressName: "my_address",
        routingType: artemis:MULTICAST
    }
}
// Attaches the service to the listener.
service artemisConsumer on new artemis:Listener({host:"localhost", port:61616}) {

    // This resource is triggered when a message is received.
    resource function onMessage(artemis:Message message) {
        // Retrieves the message payload.
        var payload = message.getPayload();
        io:print("Payload is ");
        // Checks for the type of the payload and prints the type.
        if (payload is map<string>) {
            io:println("map<string>");
        } else if (payload is map<int>) {
            io:println("int map");
        } else if (payload is string) {
            io:println("string");
        } else if (payload is byte[]) {
            io:println("byte[]");
        } else if (payload is map<byte[]>) {
            io:println("map<byte[]>");
        } else if (payload is map<boolean>) {
            io:println("map<boolean>");
        } else if (payload is map<float>) {
            io:println("map<float>");
        }
        // Prints the payload.
        io:println(payload);
    }
}
