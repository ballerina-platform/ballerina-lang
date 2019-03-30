import ballerina/artemis;
import ballerina/log;

// Creates a producer to the address "my_address" address.
artemis:Producer prod = new({host:"localhost", port:61616}, "my_address",
  addressConfig = {routingType:artemis:MULTICAST});
public function main() {
    // Sends the string message to the Artemis server.
    var err = prod->send("Hello World!");
    if (err is error) {
        log:printError("Error occurred while sending message", err = err);
    }
    // Closes the producer.
    if (!prod.isClosed()) {
        err = prod->close();
        if (err is error) {
            log:printError("Error occurred closing producer", err = err);
        }
    }
}
