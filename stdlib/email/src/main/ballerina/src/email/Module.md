This module contains functions to perform email operations such as sending and reading emails using SMTP, POP3, and IMAP4 protocols.

### Client

This module supports following three client types, 

- `email:SmtpClient` - The client that supports sending an email using the SMTP protocol.
- `email:PopClient` - The client that supports receiving an email using the POP3 protocol.
- `email:ImapClient` - The client that supports receiving an email using the IMAP4 protocol.

#### SMTP Client

To send an email using the SMTP protocol, you must first create a `email:SmtpClient` object. The code for creating a `email:SmtpClient` can be found
 below.

##### Creating a client

The following code creates a SMTP client which connects to default port(465) and enables SSL.
```ballerina
email:SmtpClient smtpClient = new ("smtp.email.com",
                                   "sender@email.com",
                                   "pass123");
```
The port number of the server and/or the SSL support can be also configured by passing following configurations,

```ballerina
email:SmtpConfig smtpConfig = {
    port: 465, // Can use ports, 465, 587 or 25
    enableSsl: true // Set true to enable SSL (SMTPS connections)
};

email:SmtpClient smtpClient = new ("smtp.email.com",
                                   "sender@email.com",
                                   "pass123",
                                    smtpConfig);
```

##### Sending an email

Once the `email:SmtpClient` is created, an email using the SMTP protocol can be sent through that client.
Samples for this operation can be found below.

```ballerina
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
```

#### POP3 Client

To receive an email using the POP3 protocol, you must first create a `email:PopClient` object. The code for creating a
 `email:PopClient` can be found below.

##### Creating a client

The following code creates a POP3 client which connects default port(995) and enables SSL.
```ballerina
email:PopClient|email:Error popClient = new ("pop.email.com",
                                             "reader@email.com",
                                             "pass456");
```

The port number of the server and/or the SSL support can be also configured by passing following configurations,
```ballerina
email:PopConfig popConfig = {
    port: 995,
    enableSsl: true
};

email:PopClient|email:Error popClient = new ("pop.email.com",
                                             "reader@email.com",
                                             "pass456",
                                              popConfig);
```

##### Receiving an email
Once the `email:PopClient` is created, emails can be received using the POP3 protocol through that client.
Samples for this operation can be found below.

```ballerina
email:Email|email:Error? emailResponse = popClient->read();
```

#### IMAP4 Client

To receive an email using the IMAP4 protocol, you must first create a `email:ImapClient` object. The code for creating a
 `email:ImapClient` can be found below.

##### Creating a client

The following code creates a IMAP4 client which connects to default port(993) and enables SSL.
```ballerina
email:ImapClient|email:Error imapClient = new ("imap.email.com",
                                               "reader@email.com",
                                               "pass456");
```

The port number of the server and/or the SSL support can be also configured by passing following configurations,
```ballerina
email:ImapConfig imapConfig = {
    port: 993,
    enableSsl: true
};

email:ImapClient|email:Error imapClient = new ("imap.email.com",
                                               "reader@email.com",
                                               "pass456",
                                                imapConfig);
```

##### Receiving an email
Once the `email:ImapClient` is created, emails can be received using the IMAP4 protocol through that client.
Samples for this operation can be found below.

```ballerina
email:Email|email:Error emailResponse = imapClient->read();
```

For information on the operation, which you can perform with this module, see the below **Functions**. For examples on the usage of the operation, see the [Send and Receive Emails Example](https://ballerina.io/learn/by-example/send-and-receive-emails.html).
