import ballerina/jms;
import ballerina/log;

// This creates a queue sender.
endpoint jms:SimpleQueueSender queueSender {
    initialContextFactory:"bmbInitialContextFactory",
    providerUrl:"amqp://admin:admin@carbon/carbon?"
                + "brokerlist='tcp://localhost:5672'",
    acknowledgementMode: "AUTO_ACKNOWLEDGE",
    queueName:"MyQueue"
};

public function main() {
    // This creates a text message.
    match (queueSender.createTextMessage("Hello from Ballerina")) {
        error e => {
            log:printError("Error occurred while creating message", err = e);
        }

        jms:Message msg => {
            // This sends the Ballerina message to the JMS provider.

            var result = queueSender->send(msg);
            if (result is error) {
               log:printError("Error occurred while sending the message", err = result);
            }
        }
    }
}
