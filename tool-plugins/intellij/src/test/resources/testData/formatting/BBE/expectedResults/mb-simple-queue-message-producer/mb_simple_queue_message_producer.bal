import ballerina/mb;
import ballerina/log;

// Create a queue sender.
endpoint mb:SimpleQueueSender queueSender {
    host: "localhost",
    port: 5672,
    queueName: "MyQueue"
};


function main(string... args) {
    // Create a Text message.
    match (queueSender.createTextMessage("Hello from Ballerina")) {
        error e => {
            log:printError("Error occurred while creating message", err = e);
        }

        mb:Message msg => {
            // Send the Ballerina message to the JMS provider.
            queueSender->send(msg) but {
               error e => log:printError("Error occurred while sending message",
                                         err = e)
            };
        }
    }
}
