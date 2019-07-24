import ballerinax/java.jms;
import ballerina/log;

// Initialize a JMS connection with the provider. This example makes use of the
// ActiveMQ Artemis broker for demonstration while it can be tried with other
// brokers that support JMS.

jms:Connection jmsConnection = new({
        initialContextFactory: 
        "org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory",
        providerUrl: "tcp://localhost:61616"
    });

// Initialize a JMS session on top of the created connection.
jms:Session jmsSession = new(jmsConnection, {
        acknowledgementMode: "SESSION_TRANSACTED"
    });

// Initialize a queue sender.
jms:QueueSender queueSender = new(jmsSession, "MyQueue");

public function main() {
    // Message is published within the `transaction` block.
    transaction {
        // Create a text message.
        var msg = new jms:Message(jmsSession, jms:TEXT_MESSAGE);
        if (msg is jms:Message) {
            var err = msg.setPayload("Hello from Ballerina");
            if (err is error) {
                log:printError("Unable to set payload" , err);
            }
            // Send the message to the JMS provider.
            var result = queueSender->send(msg);
            if (result is error) {
                log:printError("Error occurred while sending message", result);
            }
        } else {
            log:printError("Error occurred while creating message",
                err = msg);
        }
    }
}
