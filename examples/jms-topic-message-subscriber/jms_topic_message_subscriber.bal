import ballerinax/java.jms;
import ballerina/log;

// Initialize a JMS connection with the provider. Here Connection and
// Session are created explicitly to allow reusability.

jms:Connection conn = new({
        initialContextFactory:
         "org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory",
        providerUrl: "tcp://localhost:61616"
    });

// This initializes a JMS session on top of the created connection.
jms:Session jmsSession = new(conn, {
        // An optional property that defaults to AUTO_ACKNOWLEDGE.
        acknowledgementMode: "AUTO_ACKNOWLEDGE"
    });

// This initializes a topic subscriber using the created session.
listener jms:TopicListener subscriberEndpoint = new(jmsSession, "BallerinaTopic");

// Bind the created subscriber to the listener service.
service jmsListener on subscriberEndpoint {

    //This resource is invoked when a message is received.
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
