import ballerina/mb;
import ballerina/log;

// Create a topic publisher.
endpoint mb:SimpleTopicPublisher publisher {
    host: "localhost",
    port: 5672,
    topicPattern: "BallerinaTopic"
};


function main (string... args) {
    // Create a Text message.
    mb:Message m = check publisher.createTextMessage("Test Text");
    // Send the message to the Ballerina Message Broker.
    var _ = publisher->send(m);
}
