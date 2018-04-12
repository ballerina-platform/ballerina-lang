import ballerina/mb;
import ballerina/io;

// Create a queue sender
endpoint mb:SimpleQueueSender queueSender {
    host: "localhost",
    port: 5772,
    queueName: "testMbSimpleQueueReceiverProducer"
};

public function main (string[] args) {
    // Create a Text message.
    mb:Message m = check queueSender.createTextMessage("Test Text");
    // Send the Ballerina message to the JMS provider.
    var _ = queueSender -> send(m);

    io:println("Message successfully sent by mb:SimpleQueueSender");
}
