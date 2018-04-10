import ballerina/jms;
import ballerina/mb;
import ballerina/log;

// Create a queue sender
endpoint mb:SimpleQueueSender queueSender {
    initialContextFactory: "wso2mbInitialContextFactory",
    providerUrl: "amqp://admin:admin@carbon/carbon?brokerlist='tcp://localhost:5672'",
    acknowledgementMode: "AUTO_ACKNOWLEDGE",
    queueName: "MyQueue"
};


public function main (string[] args) {
    // Create a Text message.
    mb:Message m = check queueSender.createTextMessage("Test Text");
    // Send the Ballerina message to the JMS provider.
    var _ = queueSender -> send(m);
}
