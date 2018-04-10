import ballerina/mb;
import ballerina/log;

endpoint mb:SimpleQueueReceiver listener {
    host: "localhost",
    port: 5672,
    queueName: "MyQueue"
};

service<mb:Consumer> jmsListener bind listener {

    onMessage(endpoint consumer, mb:Message message) {
        string messageText = check message.getTextMessageContent();
        log:printInfo("Message : " + messageText);
    }
}
