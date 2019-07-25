import ballerinax/java.jms;
import ballerina/log;

// Create a simple queue receiver. This example makes use of the ActiveMQ
// Artemis broker for demonstration while it can be tried with other brokers
// that support JMS.

listener jms:QueueListener consumerEndpoint = new({
        initialContextFactory: 
        "org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory",
        providerUrl: "tcp://localhost:61616",
        acknowledgementMode: "AUTO_ACKNOWLEDGE"
    }, "MyQueue");

// This binds the created consumer to the listener service.
service jmsListener on consumerEndpoint {

    // This resource is invoked when a message is received.
    resource function onMessage(jms:QueueReceiverCaller consumer,
    jms:Message message) {
        // Retrieve the text message.
        var messageText = message.getPayload();
        if (messageText is string) {
            log:printInfo("Message : " + messageText);
        } else if (messageText is error) {
            log:printError("Error occurred while reading message",
                err = messageText);
        }
    }
}
