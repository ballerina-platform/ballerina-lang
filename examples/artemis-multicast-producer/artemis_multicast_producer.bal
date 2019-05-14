import ballerina/artemis;
import ballerina/io;

// Creates a producer to the address "my_address" address.
artemis:Producer prod = new({host:"localhost", port:61616}, "my_address",
  addressConfig = {routingType:artemis:MULTICAST});
public function main() {
    // Sends the string message to the Artemis server.
    error? err = prod->send("Hello World!");
    if (err is error) {
        io:println("Error occurred while sending message");
    }
    // Closes the producer.
    err = prod->close();
    if (err is error) {
        io:println("Error occurred closing producer");
    }
}
