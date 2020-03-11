import ballerina/io;
import ballerina/log;
import ballerina/nats;

// Represents the escape character.
const string ESCAPE = "!q";

// Produces a message to a subject in the NATS sever.
public function main() {
    string message = "";
    string subject = io:readln("Subject : ");

    nats:Connection conn = new;

    nats:StreamingProducer publisher = new (conn, "p0", "test-cluster");

    while (message != ESCAPE) {
        message = io:readln("Message : ");
        if (message != ESCAPE) {
            // Produces a message to the specified subject.
            var result = publisher->publish(subject, <@untainted>message);
            if (result is nats:Error) {
                error e = result;
                log:printError("Error occurred while closing the connection", e);
            } else {
                log:printInfo("GUID " + result
                                        + " received for the produced message.");
            }
        }
    }
    // Closes the connection.
    var result = conn.close();
    if (result is error) {
        error e = result;
        log:printError("Error occurred while closing the connection", e);
    }
}
