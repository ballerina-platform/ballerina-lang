import ballerina/jms;
import ballerina/log;

// This creates a simple queue receiver.
endpoint jms:SimpleQueueReceiver consumerEndpoint {
    initialContextFactory:"bmbInitialContextFactory",
    providerUrl:"amqp://admin:admin@carbon/carbon"
                + "?brokerlist='tcp://localhost:5672'",
    acknowledgementMode:"AUTO_ACKNOWLEDGE",
    queueName:"MyQueue"
};

// This binds the created consumer to the listener service.
service<jms:Consumer> jmsListener bind consumerEndpoint {

    // This resource is invoked when a message is received.
    onMessage(endpoint consumer, jms:Message message) {
        match (message.getTextMessageContent()) {
            string messageText => log:printInfo("Message : " + messageText);
            error e => log:printError("Error occurred while reading message",
                                       err=e);
        }
    }
}
