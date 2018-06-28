import ballerina/mb;
import ballerina/log;

// Create a simple durable topic subscriber.
endpoint mb:SimpleDurableTopicSubscriber subscriber {
    topicPattern: "BallerinaTopic",
    identifier: "sub-1"
};

//Bind the subscriber to the service to get the messages.
service<mb:Consumer> mbListener bind subscriber {

    //Receive the messages that come to the service.
    onMessage(endpoint consumer, mb:Message message) {
        match (message.getTextMessageContent()) {
            string messageText => log:printInfo("Message : " + messageText);
            error e => log:printError("Error occurred while reading message",
                err = e);
        }
    }
}
