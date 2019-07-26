import ballerinax/java.jms;
import ballerina/log;

// This creates a simple topic listener. This example uses the ActiveMQ
// Artemis broker. However, it can be tried with other brokers
// that support JMS.
listener jms:TopicListener subscriberEndpoint = new({
        initialContextFactory: 
        "org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory",
        providerUrl: "tcp://localhost:61616",
        acknowledgementMode: "AUTO_ACKNOWLEDGE"
    }, "MyTopic");

// Binds the created subscriber to the listener service.
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
