## Module overview

This module contains functions to perform email operations such as sending and reading emails using SMTP, POP3, and IMAP4 protocols.

## Sample

The below sample uses the functions in the module to send an email using the SMTP protocol.

```ballerina
import ballerina/email;
import ballerina/io;

email:SmtpConfig smtpConfig = {
    host: "smtp.gmail.com",
    port: 587,
    username: "sender@email.com",
    password: "pass123"
};

public function main() {
    email:SmtpClient smtpClient = new (smtpConfig);
    email:Email email = {
        to: ["receiver1@email.com", "receiver2@email.com"],
        cc: ["receiver3@email.com", "receiver4@email.com"],
        bcc: ["receiver5@email.com"],
        'from: "sender@email.com",
        subject: "Sample Email",
        body: "This is a sample email.",
        sender: "sender@email.com",
        replyTo: ["sender1@email.com", "sender2@email.com"]
    };
    error? response = smtpClient->send(email);
}
```

The below sample uses the functions in the module to receive an email using the POP3 protocol.

```ballerina
email:PopConfig popConfig = {
    host: "pop.gmail.com",
    port: 995,
    username: "sender@email.com",
    password: "senderPa$$w0rd"
};

public function main() {
    email:PopClient popClient = new (popConfig);
    email:Email|error? emailResponse = popClient->read();
    if (emailResponse is email:Email) {
        io:println("Email Subject: ", emailResponse.subject);
        io:println("Email Body: ", emailResponse.body);
    } else if (emailResponse is ()) {
        io:println("There are no emails in the INBOX.");
    } else {
        io:println("Error: " + emailResponse.toString());
    }
}
```

The below sample uses the functions in the module to receive an email using the IMAP4 protocol.

```ballerina
email:ImapConfig imapConfig = {
    host: "imap.gmail.com",
    port: 143,
    username: "sender@email.com",
    password: "senderPa$$w0rd"
};

public function main() {
    email:ImapClient imapClient = new (imapConfig);
    email:Email|error? emailResponse = imapClient->read();
    if (emailResponse is email:Email) {
        io:println("Email Subject: ", emailResponse.subject);
        io:println("Email Body: ", emailResponse.body);
    } else if (emailResponse is ()) {
        io:println("There are no emails in the INBOX.");
    } else {
        io:println("Error: " + emailResponse.toString());
    }
}
```
