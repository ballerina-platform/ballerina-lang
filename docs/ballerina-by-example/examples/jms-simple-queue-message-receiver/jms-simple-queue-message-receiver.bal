import ballerina/jms;
import ballerina/log;

// Create a simple queue receiver
endpoint jms:SimpleQueueReceiver consumer {
    initialContextFactory: "wso2mbInitialContextFactory",
    providerUrl: "amqp://admin:admin@carbon/carbon?brokerlist='tcp://localhost:5672'",
    acknowledgementMode: "AUTO_ACKNOWLEDGE",
    queueName: "MyQueue"
};

// Bind the created consumer to the listener service.
service<jms:Consumer> jmsListener bind consumer {

// OnMessage resource get invoked when a message is received.
    onMessage(endpoint consumer, jms:Message message) {
        string messageText = check message.getTextMessageContent();
        log:printInfo("Message : " + messageText);
    }
}
