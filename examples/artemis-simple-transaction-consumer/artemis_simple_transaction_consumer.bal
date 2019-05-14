import ballerina/artemis;
import ballerina/io;

public function main() returns error? {
    // Creates a simple Artemis Listener
    artemis:Listener lis = new artemis:Listener({host:"localhost", port:61616});
    // Creates a `Consumer` object to make synchronous receive calls.
    var consumer = lis.createAndGetConsumer({
        queueName: "example"
    });
    if (consumer is artemis:Consumer) {
        transaction {
            // Makes a synchronous receive call inside the transaction block.
            var msg = consumer->receive();
            if (msg is artemis:Message) {
                io:println(msg.getPayload());
            }
        }
    }
}
