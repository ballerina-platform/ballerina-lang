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
    match (queueSender.createTextMessage("Hello from Ballerina")) {
        error e => {
            log:printError("Error occurred while creating message", err = e);
        }

        jms:Message msg => {
            // Send the Ballerina message to the JMS provider.
            queueSender->send(msg) but { error e => log:printError("Error occurred while sending message", err = e) };
        }
    }
}
