import ballerina/mb;
import ballerina/log;

endpoint mb:SimpleTopicSubscriber subscriber {
    topicPattern: "BallerinaTopic"
};

service<mb:Consumer> mbListener bind subscriber {

    onMessage(endpoint consumer, mb:Message message) {
        string messageText = check message.getTextMessageContent();
        log:printInfo("Message : " + messageText);
    }
}
