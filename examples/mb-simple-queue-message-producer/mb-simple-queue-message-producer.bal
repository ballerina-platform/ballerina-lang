import ballerina/mb;
import ballerina/log;

// Create a queue sender.
endpoint mb:SimpleQueueSender queueSender {
    host: "localhost",
    port: 5672,
    queueName: "MyQueue"
};


function main (string... args) {
    // Create a Text message.
    mb:Message m = check queueSender.createTextMessage("Test Text");
    // Send the Ballerina message to the Ballerina Message Broker.
    check queueSender->send(m);
}
