import ballerina/artemis;
import ballerina/io;

public function main() {
    artemis:Producer prod = new({host:"localhost", port:61616}, "example");
    transaction {
        // Sends a message to the queue inside the transaction block.
        var err = prod->send("Hello from Ballerina!");
        if (err is error) {
            io:println("Error occurred sending message");
        }
    }
    // Closes the producer
    var err = prod->close();
    if (err is error) {
        io:println("Error occurred closing the connection");
    }
}
