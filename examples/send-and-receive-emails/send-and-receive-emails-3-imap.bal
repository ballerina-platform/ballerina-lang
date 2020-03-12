import ballerina/email;
import ballerina/io;

public function main() {
    // Creates the client with connection parameters, host, username and password.
    email:ImapClient|email:Error imapClient = new ("imap.email.com", "reader@email.com", "pass456");
    // Check whether the email is successfully received.
    if (imapClient is email:ImapClient) {
        // Read the email from the IMAP4 server.
        email:Email|email:Error emailResponse = imapClient->read();
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
            io:println("Error while getting getting response: " + <string> emailResponse.detail()["message"]);
        }
    // Print the error when an error occurred while creating the client.
    } else {
        io:println("Error while creating client: " + <string> imapClient.detail()["message"]);
    }
}
