import ballerina/io;

function sendEmail(string sender, string recipient, string subject, 
        string message) returns boolean {
    return true;
}

public function main() {
    boolean isEmailSent = sendEmail(
                                   "sender@example.com",
                                   "recipient@example.com",
                                   "This is a test email from Ballerina.",
                                   "Hello from Ballerina");
    if (isEmailSent) {
        io:println(
                  "Email sent successfully!");
    } else {
        io:println(
                  "Email sending failed.");
    }
}
