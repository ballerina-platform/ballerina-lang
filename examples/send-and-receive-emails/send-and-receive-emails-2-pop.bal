import ballerina/email;
import ballerina/io;

public function main() {
    // Create the client with the connection parameters, host, username, and password. An error is received in failure.
    // Default port number `995` is used over SSL with these configurations.
    email:PopClient|email:Error popClient = new ("pop.email.com", "reader@email.com", "pass456");
    if (popClient is email:PopClient) {
        // Read the first unseen email received by the POP3 server. Nil is returned when there are no new unseen emails.
        // In error cases an error is returned.
        email:Email|email:Error? emailResponse = popClient->read();
        if(emailResponse is email:Email) {
            io:println("Email Subject: ", emailResponse.subject);
            io:println("Email Body: ", emailResponse.body);
        // When no emails are available in the server, nil is returned.
        } else if (emailResponse is ()) {
            io:println("There are no emails in the INBOX.");
        } else {
            io:println("Error while getting getting response: " + <string> emailResponse.detail()["message"]);
        }
    } else {
        io:println("Error while creating client: " + <string> popClient.detail()["message"]);
    }
}
