import ballerina/jms;
import ballerina/io;

// Create a queue sender
endpoint jms:SimpleQueueSender queueSender {
    initialContextFactory: "bmbInitialContextFactory",
    providerUrl: "amqp://admin:admin@carbon/carbon?brokerlist='tcp://localhost:5772'",
    queueName: "testMbSimpleQueueReceiverProducer"
};

public function main () {
    // Create a Text message.
    var msg = queueSender.createTextMessage("Test Text");
    if (msg is jms:Message) {
         // Send the Ballerina message to the JMS provider.
         _ = queueSender -> send(msg);
    } else {
         panic msg;
    }

    io:println("Message successfully sent by jms:SimpleQueueSender");
}
