import ballerina/artemis;
import ballerina/io;

// Creates the Connection and Session explicitly that allows reusability
// when necessary.
artemis:Connection con = new("tcp://localhost:61616");
artemis:Session session = new(con);

// Consumer listens to the "hello" queue.
@artemis:ServiceConfig {
    queueConfig: {
        queueName: "queue1"
    }
}
// Attaches the service to the listener created using the session.
service artemisConsumer on new artemis:Listener(session) {
    // This resource is triggered when a message is received.
    resource function onMessage(artemis:Message message) {
        // Retrieves the message payload.
        var payload = message.getPayload();
        io:print("Payload is ");
        // Checks for the type of the payload and prints the type.
        if (payload is map<string>) {
            io:println("map<string>");
        } else if (payload is string) {
            io:println("string");
        }
        // Prints the payload.
        io:println(payload);
    }
}
