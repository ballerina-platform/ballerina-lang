import ballerina/mb;
import ballerina/io;

// Create a queue sender
endpoint mb:SimpleTopicPublisher publisher {
    host: "localhost",
    port: 5772,
    topicPattern: "testMbSimpleTopicSubscriberPublisher"
};

function main (string... args) {
    // Create a Text message.
    mb:Message m = check publisher.createTextMessage("Test Text");
    // Send the Ballerina message to the JMS provider.
    var _ = publisher -> send(m);

    io:println("Message successfully sent by mb:SimpleTopicPublisher");
}
