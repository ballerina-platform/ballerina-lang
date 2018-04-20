import ballerina/jms;
import ballerina/log;

// Create a queue sender.
endpoint jms:SimpleQueueSender queueSender {
    initialContextFactory: "wso2mbInitialContextFactory",
    providerUrl: "amqp://admin:admin@carbon/carbon?brokerlist='tcp://localhost:5672'",
    acknowledgementMode: "AUTO_ACKNOWLEDGE",
    queueName: "MyQueue"
};


function main (string... args) {
    // Create a Text message.
    jms:Message m = check queueSender.createTextMessage("Test Text");
    // Send the Ballerina message to the JMS provider.
    check queueSender->send(m);
    log:printInfo("Message successfully sent.");
}
