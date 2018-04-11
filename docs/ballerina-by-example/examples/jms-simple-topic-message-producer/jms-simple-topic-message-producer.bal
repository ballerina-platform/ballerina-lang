import ballerina/jms;
import ballerina/log;

// Create a topic publisher
endpoint jms:SimpleTopicPublisher topicPublisher {
    initialContextFactory: "wso2mbInitialContextFactory",
    providerUrl: "amqp://admin:admin@carbon/carbon?brokerlist='tcp://localhost:5672'",
    acknowledgementMode: "AUTO_ACKNOWLEDGE",
    topicPattern: "BallerinaTopic"
};

public function main (string[] args) {
    // Create a Text message.
    jms:Message m = check topicPublisher.createTextMessage("Test Text");
    // Send the Ballerina message to the JMS provider.
    var _ = topicPublisher -> send(m);
}
