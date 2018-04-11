import ballerina/jms;
import ballerina/log;

// Create a simple durable topic listener.
endpoint jms:SimpleDurableTopicListener subscriber {
    initialContextFactory: "wso2mbInitialContextFactory",
    providerUrl: "amqp://admin:admin@carbon/carbon?brokerlist='tcp://localhost:5672'",
    acknowledgementMode: "AUTO_ACKNOWLEDGE",
    topicPattern: "BallerinaTopic",
    identifier: "sub1"
};

// Bind the created consumer to the listener service.
service<jms:Consumer> jmsListener bind subscriber {

    // The OnMessage resource is invoked when a message is received.
    onMessage(endpoint consumer, jms:Message message) {
        string messageText = message.getTextMessageContent();
        log:printInfo("Message : " + messageText);
  }
}
