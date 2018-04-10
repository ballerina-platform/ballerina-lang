import ballerina/jms;
import ballerina/log;

// Create a simple topic listener
endpoint jms:SimpleTopicListener subscriber {
    initialContextFactory: "wso2mbInitialContextFactory",
    providerUrl: "amqp://admin:admin@carbon/carbon?brokerlist='tcp://localhost:5672'",
    acknowledgementMode: "AUTO_ACKNOWLEDGE",
    topicPattern: "BallerinaTopic"
};

// Bind the created consumer to the listener service.
service<jms:Consumer> jmsListener bind subscriber {

    // OnMessage resource get invoked when a message is received.
    onMessage(endpoint consumer, jms:Message message) {
        string messageText = message.getTextMessageContent();
        log:printInfo("Message : " + messageText);
  }
}
