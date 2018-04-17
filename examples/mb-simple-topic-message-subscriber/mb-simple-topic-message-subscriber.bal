import ballerina/mb;
import ballerina/log;

// Create a simple topic subscriber to listen to the `BallerinaTopic`.
endpoint mb:SimpleTopicSubscriber subscriber {
    topicPattern: "BallerinaTopic"
};

// Bind the subscriber to the service.
service<mb:Consumer> mbListener bind subscriber {

    // Receive the messages that comes to the service.
    onMessage(endpoint consumer, mb:Message message) {
        string messageText = check message.getTextMessageContent();
        log:printInfo("Message : " + messageText);
    }
}
