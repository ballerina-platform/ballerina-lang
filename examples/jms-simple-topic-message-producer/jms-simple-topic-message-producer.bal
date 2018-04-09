import ballerina/jms;
import ballerina/log;

// Create a topic producer
endpoint jms:SimpleTopicProducer topicProducer {
    initialContextFactory: "wso2mbInitialContextFactory",
    providerUrl: "amqp://admin:admin@carbon/carbon?brokerlist='tcp://localhost:5672'",
    acknowledgementMode: "AUTO_ACKNOWLEDGE",
    topicPattern: "BallerinaTopic"
};

public function main (string[] args) {
    // Create a Text message.
    jms:Message m = topicProducer.createTextMessage("Test Text");
    // Send the Ballerina message to the JMS provider.
    topicProducer -> send(m);
}
