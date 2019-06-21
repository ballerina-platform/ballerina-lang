import ballerina/io;
import ballerina/log;
import ballerina/nats;

// Represents the escape character.
const string ESCAPE = "!q";

// Produces a message to a subject in the NATS sever.
public function main() {
    string message = "";
    string subject = io:readln("Subject : ");
    // Initializes a producer.
    nats:Producer producer = new({ host: "localhost", port: 4222, clientId: "p0" });
    while (message != ESCAPE) {
        message = io:readln("Message : ");
        if (message != ESCAPE) {
            // Produces a message to the specified subject.
            var result = producer->send(subject, message);
            if (result is error) {
                io:println("Error occurred while producing the message.");
            } else {
                io:println("GUID " + result + " received for the produced message.");
            }
        }
    }
    // Closes the publisher connection.
    var result = producer.close();
    if (result is error) {
        log:printError("Error occurred while closing the connection", err = result);
    }
}
