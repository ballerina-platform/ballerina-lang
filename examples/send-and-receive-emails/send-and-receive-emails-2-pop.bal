import ballerina/email;
import ballerina/io;

public function main() {
    // Create the client with the connection parameters, host, username, and password. An error is received in failure.
    email:PopClient|email:Error popClient = new ("pop.email.com", "reader@email.com", "pass456");
    // Check whether the email is received successfully. Otherwise, print the error message.
    if (popClient is email:PopClient) {
        // Read the first unseen email received by the POP3 server. In error scenarios an error is returned.
        email:Email|email:Error? emailResponse = popClient->read();
        // Check whether the email is received successfully. Otherwise, print the error message.
        if(emailResponse is email:Email) {
            // Print the subject of the email.
            io:println("Email Subject: ", emailResponse.subject);
            // Print the body of the email.
            io:println("Email Body: ", emailResponse.body);
        // When no emails are available in the server, nil is returned.
        } else if (emailResponse is ()) {
            io:println("There are no emails in the INBOX.");
        // Print the error when an error occurred while receiving the email.
        } else {
            io:println("Error while getting getting response: " + <string> emailResponse.detail()["message"]);
        }
    // Print the error when an error occurred while creating the client.
    } else {
        io:println("Error while creating client: " + <string> popClient.detail()["message"]);
    }
}
