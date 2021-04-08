import ballerina/email;
import ballerina/io;

public function main() returns error? {
    // Creates an SMTP client with the connection parameters, host, username,
    // and password. The default port number `465` is used over SSL with these
    // configurations.
    email:SmtpClient smtpClient = new ("smtp.email.com", "sender@email.com"
        , "pass123");

    // Define the email that is required to be sent.
    email:Email email = {
        // "TO", "CC", and "BCC" address lists are added as follows.
        // Only "TO" address list is mandatory out of these three.
        to: ["receiver1@email.com", "receiver2@email.com"],
        cc: ["receiver3@email.com", "receiver4@email.com"],
        bcc: ["receiver5@email.com"],
        // Subject of the email is added as follows. This field is mandatory.
        subject: "Sample Email",
        // Body content of the email is added as follows.
        // This field is mandatory.
        body: "This is a sample email.",
        // Email author's address is added as follows. This field is mandatory.
        'from: "author@email.com",
        // Email sender service address is added as follows.
        // This field is optional. `Sender` is same as the `'from` when the
        // email author himself sends the email.
        sender: "sender@email.com",
        // List of recipients when replying to the email is added as follows.
        // This field is optional. These addresses are required when the emails
        // are to be replied to some other address(es) other than the sender or
        // the author.
        replyTo: ["replyTo1@email.com", "replyTo2@email.com"]
    };

    // Send the email with the client.
    check smtpClient->send(email);

    // Create the client with the connection parameters, host, username, and
    // password. An error is received in a failure. The default port number
    // `995` is used over SSL with these configurations.
    email:PopClient popClient = new ("pop.email.com", "reader@email.com",
        "pass456");

    // Read the first unseen email received by the POP3 server. `()` is
    // returned when there are no new unseen emails. In error cases, an
    // error is returned.
    email:Email? emailResponse = check popClient->read();

    if (emailResponse is email:Email) {
        io:println("Email Subject: ", emailResponse.subject);
        io:println("Email Body: ", emailResponse.body);
    // When no emails are available in the server, `()` is returned.
    } else {

        io:println("There are no emails in the INBOX.");
    }

    // Create the client with the connection parameters, host, username, and
    // password. An error is received in a failure. The default port number
    // `993` is used over SSL with these configurations.
    email:ImapClient imapClient = new ("imap.email.com", "reader@email.com",
        "pass456");

    // Read the first unseen email received by the IMAP4 server. `()` is
    // returned when there are no new unseen emails. In error cases, an
    // error is returned.
    emailResponse = check imapClient->read();

    if (emailResponse is email:Email) {
        io:println("Email Subject: ", emailResponse.subject);
        io:println("Email Body: ", emailResponse.body);
    // When no emails are available in the server, `()` is returned.
    } else {

        io:println("There are no emails in the INBOX.");
    }

}

// Defines the protocol-specific configuration for the email listener. It can
// either be `email:PopConfig` for POP or `email:ImapConfig` for IMAP.
email:PopConfig popConfig = {
     port: 995,
     enableSsl: true
};

// Create the listener with the connection parameters and protocol-related 
// configuration. The polling interval specifies the time duration between each poll
// performed by the listener.
listener email:Listener emailListener = new ({
    host: "pop.email.com",
    username: "reader@email.com",
    password: "pass456",
    protocol: "POP",
    protocolConfig: popConfig,
    pollingInterval: 2000
});

// One or many services can listen to the email listener for the periodically-polled
// emails.
service emailObserver on emailListener {

    // When an email is successfully received, the `onMessage` method is called.
    resource function onMessage(email:Email emailMessage) {
        io:println("Email Subject: ", emailMessage.subject);
        io:println("Email Body: ", emailMessage.body);
    }

    // When an error occurs during the email poll operations, the `onError` method is called.
    resource function onError(email:Error emailError) {
        io:println("Error while polling for the emails: "
            + emailError.message());
    }

}
