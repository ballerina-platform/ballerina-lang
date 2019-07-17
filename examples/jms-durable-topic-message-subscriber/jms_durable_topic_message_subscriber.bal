import ballerinax/java.jms;
import ballerina/log;

// This initializes a JMS connection with the provider. This example uses
// the ActiveMQ Artemis broker. However, it can be tried with
// other brokers that support JMS.

jms:Connection conn = new({
        initialContextFactory: 
        "org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory",
        providerUrl: "tcp://localhost:61616"
    });

// Initializes a JMS session on top of the created connection.
jms:Session jmsSession = new(conn, {
        // The below is an optional property that defaults to `AUTO_ACKNOWLEDGE`.
        acknowledgementMode: "AUTO_ACKNOWLEDGE"
    });

// Initializes a durable topic subscriber using the created session.
listener jms:DurableTopicListener subscriberEndpoint = new(jmsSession,
    "MyTopic", "sub1");

// Binds the created consumer to the listener service.
service jmsListener on subscriberEndpoint {

    // This resource is invoked when a message is received.
    resource function onMessage(jms:TopicSubscriberCaller consumer,
                                jms:Message message) {
        // Retrieves the text message.
        var messageText = message.getPayload();
        if (messageText is string) {
            log:printInfo("Message : " + messageText);
        } else if (messageText is error) {
            log:printError("Error occurred while reading message",
                err = messageText);
        }
    }
}
