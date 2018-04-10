import ballerina/jms;
import ballerina/mb;
import ballerina/log;

// Create a topic publisher
endpoint mb:SimpleTopicPublisher topicPublisher {
    host: "localhost",
    port: 5672,
    topicPattern: "BallerinaTopic"
};


public function main (string[] args) {
    // Create a Text message.
    mb:Message m = check topicPublisher.createTextMessage("Test Text");
    // Send the Ballerina message to the JMS provider.
    var _ = topicPublisher -> send(m);
}
