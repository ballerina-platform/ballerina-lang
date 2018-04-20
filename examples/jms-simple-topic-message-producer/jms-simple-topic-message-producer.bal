import ballerina/jms;
import ballerina/log;

// Create a topic publisher.
endpoint jms:SimpleTopicPublisher topicPublisher {
    initialContextFactory: "wso2mbInitialContextFactory",
    providerUrl: "amqp://admin:admin@carbon/carbon?brokerlist='tcp://localhost:5672'",
    acknowledgementMode: "AUTO_ACKNOWLEDGE",
    topicPattern: "BallerinaTopic"
};

function main (string... args) {
    // Create a Text message.
    jms:Message m = check topicPublisher.createTextMessage("Test Text");
    // Send the Ballerina message to the JMS provider.
    check topicPublisher->send(m);
    log:printInfo("Message successfully sent.");
}
