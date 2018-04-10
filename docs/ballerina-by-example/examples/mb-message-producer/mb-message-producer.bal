import ballerina/jms;
import ballerina/mb;

endpoint mb:ClientEndpoint mbEP {
    brokerUrl: "amqp://admin:admin@carbon/carbon?brokerlist='tcp://localhost:5672'"
};

function main (string[] args) {
    // Create a Text message.
    jms:Message queueMessage = mbEP.createTextMessage("Hello from Ballerina!");
    // Send the Ballerina message to the JMS provider.
    mbEP->send("MyQueue", queueMessage);
}
