import ballerinax/java.jms;
import ballerina/log;

// This creates a simple durable topic subscriber.  This example makes use of
// the ActiveMQ Artemis broker for demonstration while it can be tried with
// other brokers that support JMS.
listener jms:DurableTopicListener subscriberEndpoint = new({
        initialContextFactory: 
        "org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory",
        providerUrl: "tcp://localhost:61616",
        acknowledgementMode: "AUTO_ACKNOWLEDGE"
    }, "BallerinaTopic", "sub1");

// Bind the created consumer to the listener service.
service jmsListener on subscriberEndpoint {

    // This resource is invoked when a message is received.
    resource function onMessage(jms:TopicSubscriberCaller consumer,
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
