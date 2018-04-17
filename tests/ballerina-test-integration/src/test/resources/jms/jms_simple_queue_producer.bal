import ballerina/jms;
import ballerina/io;

// Create a queue sender
endpoint jms:SimpleQueueSender queueSender {
    initialContextFactory: "wso2mbInitialContextFactory",
    providerUrl: "amqp://admin:admin@carbon/carbon?brokerlist='tcp://localhost:5772'",
    queueName: "testMbSimpleQueueReceiverProducer"
};

function main (string... args) {
    // Create a Text message.
    jms:Message m = check queueSender.createTextMessage("Test Text");
    // Send the Ballerina message to the JMS provider.
    var _ = queueSender -> send(m);

    io:println("Message successfully sent by jms:SimpleQueueSender");
}
