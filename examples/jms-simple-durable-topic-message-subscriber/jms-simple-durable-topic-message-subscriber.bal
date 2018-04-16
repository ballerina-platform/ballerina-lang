import ballerina/jms;
import ballerina/log;

// Create a simple durable topic subscriber.
endpoint jms:SimpleDurableTopicSubscriber subscriber {
    initialContextFactory: "wso2mbInitialContextFactory",
    providerUrl: "amqp://admin:admin@carbon/carbon?brokerlist='tcp://localhost:5672'",
    acknowledgementMode: "AUTO_ACKNOWLEDGE",
    topicPattern: "BallerinaTopic",
    identifier: "sub1"
};

// Bind the created subscriber to the listener service.
service<jms:Consumer> jmsListener bind subscriber {

    // The `OnMessage` resource is invoked when a message is received.
    onMessage(endpoint consumer, jms:Message message) {
        string messageText = check message.getTextMessageContent();
        log:printInfo("Message : " + messageText);
  }
}
