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
    nats:Connection connection = new();
    nats:Producer producer = new(connection);
    while (message != ESCAPE) {
        message = io:readln("Message : ");
        // Produces a message to the specified subject.
        nats:Error? result = producer->publish(subject, <@untainted>message);
        if (result is nats:Error) {
            io:println("Error occurred while producing the message.");
        } else {
            io:println("Message published successfully.");
        }
    }
    // Closes the publisher connection.
    nats:Error? result = producer.close();
    if (result is nats:Error) {
        log:printError("Error occurred while closing the logical connection",
                                                                        result);
    }

    result = connection.close();
    if (result is nats:Error) {
        log:printError("Error occurred while closing the connection", result);
    }
}
