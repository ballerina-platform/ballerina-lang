import ballerina/jms;
import ballerina/io;

// Create a queue sender
endpoint jms:SimpleQueueSender queueSender {
    initialContextFactory: "bmbInitialContextFactory",
    providerUrl: "amqp://admin:admin@carbon/carbon?brokerlist='tcp://localhost:5772'",
    queueName: "MyQueue"
};

function main (string... args) {
    // Create a Text message.
    jms:Message m = check queueSender.createTextMessage("Test Text");
    check m.setBooleanProperty("booleanProp", false);
    check m.setIntProperty("intProp", 10);
    check m.setFloatProperty("floatProp", 10.5);
    check m.setStringProperty("stringProp", "TestString");
    // Send the Ballerina message to the JMS provider.
    var _ = queueSender -> send(m);

    io:println("Message successfully sent by jms:SimpleQueueSender");
}
