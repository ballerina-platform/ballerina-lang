import ballerina/mb;
import ballerina/log;

// Create a simple queue receiver.
endpoint mb:SimpleQueueReceiver receiver {
    host: "localhost",
    port: 5672,
    queueName: "MyQueue"
};

// Bind the receiver to the queue to get the messages.
service<mb:Consumer> mbListener bind receiver {

    // Receive the messages that comes to the queue.
    onMessage(endpoint consumer, mb:Message message) {
        match (message.getTextMessageContent()) {
            string messageText => log:printInfo("Message : " + messageText);
            error e => log:printError("Error occurred while reading message",
                                      err = e);
        }
    }
}
