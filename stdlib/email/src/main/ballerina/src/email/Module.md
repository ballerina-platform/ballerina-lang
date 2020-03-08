## Module overview

This module contains functions to perform email operations such as sending and reading emails using SMTP, POP3, and IMAP4 protocols.

## Sample

The below sample uses the functions in the module to send an email using the SMTP protocol.

```ballerina
import ballerina/email;
import ballerina/io;

email:SmtpConfig smtpConfig = {
    port: 465, // Can use ports, 465, 587 or 25
    enableSsl: true // Set true to enable SSL (SMTPS connections)
};

public function main() {
    email:SmtpClient smtpClient = new ("smtp.email.com", "sender@email.com", "pass123", smtpConfig);
    email:Email email = {
        to: ["receiver1@email.com", "receiver2@email.com"],
        cc: ["receiver3@email.com", "receiver4@email.com"],
        bcc: ["receiver5@email.com"],
        subject: "Sample Email",
        body: "This is a sample email.",
        'from: "author@email.com",
        sender: "sender@email.com",
        replyTo: ["replyTo1@email.com", "replyTo2@email.com"]
    };

    email:EmailSendError? response = smtpClient->send(email);
    if (response is email:EmailSendError) {
        io:println("Error while sending the email: " + response.toString());
    }

}
```

The below sample uses the functions in the module to receive an email using the POP3 protocol.

```ballerina
import ballerina/email;
import ballerina/io;

email:PopConfig popConfig = {
    port: 995,
    enableSsl: true
};

public function main() {
    email:PopClient|email:GetStoreError popClient = 
        new ("pop.email.com", "reader@email.com", "pass456", popConfig);
    if (popClient is email:PopClient) {
        email:Email|email:EmailReadError? emailResponse = popClient->read();
        if(emailResponse is email:Email) {
            io:println("Email Subject: ", emailResponse.subject);
            io:println("Email Body: ", emailResponse.body);
        } else if (emailResponse is ()) {
            io:println("There are no emails in the INBOX.");
        } else {
            io:println("Error while getting getting response: " + emailResponse.toString());
        }
    } else {
        io:println("Error while creating client: " + popClient.toString());
    }
}
```

The below sample uses the functions in the module to receive an email using the IMAP4 protocol.

```ballerina
import ballerina/email;
import ballerina/io;

email:ImapConfig imapConfig = {
    port: 993,
    enableSsl: true
};

public function main() {
    email:ImapClient|email:GetStoreError imapClient = 
        new ("imap.email.com", "reader@email.com", "pass456", imapConfig);
    if (imapClient is email:ImapClient) {
            email:Email|email:EmailReadError? emailResponse = imapClient->read();
        if(emailResponse is email:Email) {
            io:println("Email Subject: ", emailResponse.subject);
            io:println("Email Body: ", emailResponse.body);
        } else if (emailResponse is ()) {
            io:println("There are no emails in the INBOX.");
        } else {
            io:println("Error while getting getting response: " + emailResponse.toString());
        }
    } else {
        io:println("Error while creating client: " + imapClient.toString());
    }
}
```
