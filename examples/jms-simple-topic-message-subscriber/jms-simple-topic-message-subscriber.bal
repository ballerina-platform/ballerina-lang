import ballerina/jms;
import ballerina/log;

// Create a simple topic listener.
endpoint jms:SimpleTopicSubscriber subscriber {
    initialContextFactory: "wso2mbInitialContextFactory",
    providerUrl: "amqp://admin:admin@carbon/carbon?brokerlist='tcp://localhost:5672'",
    acknowledgementMode: "AUTO_ACKNOWLEDGE",
    topicPattern: "BallerinaTopic"
};

// Bind the created subscriber to the listener service.
service<jms:Consumer> jmsListener bind subscriber {

    // The `OnMessage` resource gets invoked when a message is received.
    onMessage(endpoint consumer, jms:Message message) {
        match (message.getTextMessageContent()) {
            string messageText => log:printInfo("Message : " + messageText);
            error e => log:printError("Error occurred while reading message", err=e);
        }
  }
}
