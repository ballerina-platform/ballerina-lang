import ballerina/email;
import ballerina/io;

public function main() {
    // Creates the client with connection parameters, host, username and password.
    email:PopClient|email:Error popClient = new ("pop.email.com", "reader@email.com", "pass456");
    // Check whether the email is successfully received.
    if (popClient is email:PopClient) {
        // Read the email from the POP3 server.
        email:Email|email:Error? emailResponse = popClient->read();
        // Check whether the email received successfully.
        if(emailResponse is email:Email) {
            // Print the subject of the email.
            io:println("Email Subject: ", emailResponse.subject);
            // Print the body of the email.
            io:println("Email Body: ", emailResponse.body);
        // When no emails are available in the server, nil is returned.
        } else if (emailResponse is ()) {
            io:println("There are no emails in the INBOX.");
        // Print the error when an error occurred during the email receive operation.
        } else {
            io:println("Error while getting getting response: " + emailResponse.toString());
        }
    // Print the error when an error occurred while creating the client.
    } else {
        io:println("Error while creating client: " + popClient.toString());
    }
}