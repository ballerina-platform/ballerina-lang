import ballerina/nats;
import ballerina/io;

// Represent escape character
const string ESCAPE = "!q";

// Produce a message to a subject in NATS sever.
public function main() {
    string message = "";
    string subject = io:readln("Subject : ");
    // Initializes a producer
    nats:Producer producer = new({ host: "localhost", port: 4222, clientId: "p0" });
    while (message != ESCAPE) {
        message = io:readln("Message : ");
        if (message != ESCAPE) {
            var result = producer->send(subject, message);
            if (result is error) {
                io:println("Error occurred while producing the message.");
            } else {
                io:println("GUID "+result+" received for the produced message.");
            }
        }
    }
}
