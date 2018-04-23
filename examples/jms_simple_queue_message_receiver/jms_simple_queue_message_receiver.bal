import ballerina/jms;
import ballerina/log;

// Create a simple queue receiver.
endpoint jms:SimpleQueueReceiver consumer {
    initialContextFactory: "bmbInitialContextFactory",
    providerUrl: "amqp://admin:admin@carbon/carbon?brokerlist='tcp://localhost:5672'",
    acknowledgementMode: "AUTO_ACKNOWLEDGE",
    queueName: "MyQueue"
};

// Bind the created consumer to the listener service.
service<jms:Consumer> jmsListener bind consumer {

// The `OnMessage` resource gets invoked when a message is received.
    onMessage(endpoint consumer, jms:Message message) {
        match (message.getTextMessageContent()) {
            string messageText => log:printInfo("Message : " + messageText);
            error e => log:printError("Error occurred while reading message", err=e);
        }
    }
}
