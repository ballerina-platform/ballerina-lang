import ballerina/artemis;
import ballerina/io;

public function main() {
    artemis:Connection con = new("tcp://localhost:61616");
    // Create the Artemis `Session` with the `autoCommitSends` and
    // the `autoCommitAcks`properties set to `false`.
    // In this case, the message acks and sends will be committed only
    // if the transaction block completes successfully.
    artemis:Session session = new(con,
                config = {autoCommitSends: false, autoCommitAcks:  false});
    artemis:Producer prod = new(session, "example");
    transaction {
        // Sends a message to the queue inside the transaction block.
        var err = prod->send("Hello from Ballerina!");
        if (err is error) {
            io:println("Error occurred sending message");
        }
    }

    // Closes the connection
    error? err = con->close();
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
        io:println("Error occurred closing the connection");
    }
}
