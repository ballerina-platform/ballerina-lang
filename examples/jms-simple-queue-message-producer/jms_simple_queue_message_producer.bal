import ballerinax/jms;
import ballerina/log;

// This creates a queue sender. This example makes use of the ActiveMQ Artemis
// broker for demonstration while it can be tried with other brokers that
// support JMS.
jms:QueueSender queueSender = new({
        initialContextFactory: 
        "org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory",
        providerUrl: "tcp://localhost:61616",
        acknowledgementMode: "AUTO_ACKNOWLEDGE"
    }, queueName = "MyQueue");

public function main() {
    // This creates a text message.
    var msg = queueSender.session.createTextMessage("Hello from Ballerina");
    if (msg is jms:Message) {
        // This sends the Ballerina message to the JMS provider.
        var returnVal = queueSender->send(msg);
        if (returnVal is error) {
            log:printError("Error occurred while sending message",
                err = returnVal);
        }
    } else {
        log:printError("Error occurred while creating message",
            err = msg);
    }
}
