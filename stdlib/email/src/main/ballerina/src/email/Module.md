## Module overview

This module contains functions to perform email operations such as sending and reading emails using SMTP, POP3, and IMAP4 protocols.

## Sample

The below sample uses the functions in the module to send an email using the SMTP protocol.

```ballerina
import ballerina/email;
import ballerina/io;

public function main() {
    email:SmtpClient smtpClient = new ("smtp.email.com", "sender@email.com", "pass123");
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

    email:Error? response = smtpClient->send(email);
    if (response is email:Error) {
        io:println("Error while sending the email: " + response.toString());
    }

}
```

Port number of the server and/or the SSL support can be also configured with a configuration as follows.

```ballerina
email:SmtpConfig smtpConfig = {
    port: 465, // Can use ports, 465, 587 or 25
    enableSsl: true // Set true to enable SSL (SMTPS connections)
};

email:SmtpClient smtpClient = new ("smtp.email.com", "sender@email.com", "pass123", smtpConfig);
```


The below sample uses the functions in the module to receive an email using the POP3 protocol.

```ballerina
import ballerina/email;
import ballerina/io;

public function main() {
    email:PopClient|email:Error popClient = new ("pop.email.com", "reader@email.com", "pass456");
    if (popClient is email:PopClient) {
        email:Email|email:Error? emailResponse = popClient->read();
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

Port number of the server and/or the SSL support can be also configured with a configuration as follows.

```ballerina
email:PopConfig popConfig = {
    port: 995,
    enableSsl: true
};

email:PopClient|email:Error popClient = new ("pop.email.com", "reader@email.com", "pass456", popConfig);
```

The below sample uses the functions in the module to receive an email using the IMAP4 protocol.

```ballerina
import ballerina/email;
import ballerina/io;

public function main() {
    email:ImapClient|email:Error imapClient = new ("imap.email.com", "reader@email.com", "pass456");
    if (imapClient is email:ImapClient) {
            email:Email|email:Error emailResponse = imapClient->read();
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

Port number of the server and/or the SSL support can be also configured with a configuration as follows.

```ballerina
email:ImapConfig imapConfig = {
    port: 993,
    enableSsl: true
};

email:ImapClient|email:Error imapClient = new ("imap.email.com", "reader@email.com", "pass456", imapConfig);
```
