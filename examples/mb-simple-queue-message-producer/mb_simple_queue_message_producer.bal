import ballerina/log;
import ballerina/mb;

// Create a queue sender.
endpoint mb:SimpleQueueSender queueSender {
    host: "localhost",
    port: 5672,
    queueName: "MyQueue"
};


public function main() {
    // Create a Text message.
    match (queueSender.createTextMessage("Hello from Ballerina")) {
        error e => {
            log:printError("Error occurred while creating message", err = e);
        }

        mb:Message msg => {
            // Send the Ballerina message to the JMS provider.
            var result = queueSender->send(msg);
            if (result is error) {
               log:printError("Error occurred while sending the response", err = result);
            }
        }
    }
}
