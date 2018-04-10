import ballerina/jms;
import ballerina/mb;
import ballerina/log;

// Create a topic producer
endpoint mb:SimpleTopicProducer topicProducer {
    host: "localhost",
    port: 5672,
    topicPattern: "BallerinaTopic"
};


public function main (string[] args) {
    // Create a Text message.
    mb:Message m = check topicProducer.createTextMessage("Test Text");
    // Send the Ballerina message to the JMS provider.
    var _ = topicProducer -> send(m);
}
