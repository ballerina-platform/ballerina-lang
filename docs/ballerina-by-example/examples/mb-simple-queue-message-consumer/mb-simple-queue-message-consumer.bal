import ballerina/mb;
import ballerina/log;

endpoint mb:SimpleQueueReceiver listener {
    queueName: "myQueue"
};

service<mb:Consumer> jmsListener bind listener {

    onMessage(endpoint consumer, mb:Message message) {
        string messageText = check message.getTextMessageContent();
        log:printInfo("Message : " + messageText);
    }
}
