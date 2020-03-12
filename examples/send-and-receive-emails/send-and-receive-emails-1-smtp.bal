import ballerina/email;
import ballerina/io;

public function main() {
    // Creates an SMTP client with connection parameters, host, username and password.
    email:SmtpClient smtpClient = new ("smtp.email.com", "sender@email.com", "pass123");

    // Defines the email that is required to be sent.
    email:Email email = {
        // "TO" address list is added as follows.
        to: ["receiver1@email.com", "receiver2@email.com"],
        // "CC" address list is added as follows.
        cc: ["receiver3@email.com", "receiver4@email.com"],
        // "BCC" address list is added as follows.
        bcc: ["receiver5@email.com"],
        // Subject of the email is added as follows.
        subject: "Sample Email",
        // Body content of the email is added as follows.
        body: "This is a sample email.",
        // Email author's address is added as follows.
        'from: "author@email.com",
        // Email sender service address is added as follows.
        sender: "sender@email.com",
        // List of recipients when replying to the email is added as follows.
        replyTo: ["replyTo1@email.com", "replyTo2@email.com"]
    };

    // Send the email with the client.
    email:Error? response = smtpClient->send(email);
    // If the email send failed, the error is handled.
    if (response is email:Error) {
        io:println("Error while sending the email: " + response.toString());
    }

}
