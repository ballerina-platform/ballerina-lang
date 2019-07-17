import ballerina/artemis;
import ballerina/io;

public function main() {
    // Establishes a connection with the broker
    artemis:Connection con = new("tcp://localhost:61616");
    // Creates the Session using the Connection object.
    // Creating the Connection and Session explicitly allows reusability.
    artemis:Session session = new(con);
    map<string> mapContent = {
        "name": "John",
        "age": "25"
    };
    // Creates a message object using the Session with a map object as the
    // content.
    artemis:Message msg = new(session, mapContent);
    // Creates a producer to the "hello" address using the Session object.
    artemis:Producer prod = new(session, "queue1", { autoCreated:false });

    // Sends the message as a string to the Artemis server.
    error? err = prod->send(msg);
    if (err is error) {
        io:println("Error occurred while sending message");
    }

    // Closes the connection
    err = con->close();
    if (err is error) {
        io:println("Error occurred closing connection");
    }
    // Closes the session
    err = session->close();
    if (err is error) {
        io:println("Error occurred closing session");
    }
    // Closes the producer.
    err = prod->close();
    if (err is error) {
        io:println("Error occurred closing producer");
    }
}
