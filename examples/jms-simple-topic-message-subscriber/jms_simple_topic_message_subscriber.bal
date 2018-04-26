import ballerina/jms;
import ballerina/log;

// This creates a simple topic listener.
endpoint jms:SimpleTopicSubscriber subscriber {
    initialContextFactory:"bmbInitialContextFactory",
    providerUrl:"amqp://admin:admin@carbon/carbon?"
                + "brokerlist='tcp://localhost:5672'",
    acknowledgementMode:"AUTO_ACKNOWLEDGE",
    topicPattern:"BallerinaTopic"
};

// This binds the created subscriber to the listener service.
service<jms:Consumer> jmsListener bind subscriber {

    // This resource is invoked when a message is received.
    onMessage(endpoint consumer, jms:Message message) {
        match (message.getTextMessageContent()) {
            string messageText => log:printInfo("Message : " + messageText);
            error e => log:printError("Error occurred while reading message",
                                      err=e);
        }
    }
}
