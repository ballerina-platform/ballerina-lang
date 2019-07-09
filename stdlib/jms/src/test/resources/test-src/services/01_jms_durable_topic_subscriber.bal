import ballerinax/jms;

string msgVal = "";
// Initialize a JMS connection with the provider.
jms:Connection conn = new ({
        initialContextFactory: "org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory",
        providerUrl: "tcp://localhost:61616"
    });

// Initialize a JMS session on top of the created connection.
jms:Session jmsSession = new (conn, {
        // Optional property. Defaults to AUTO_ACKNOWLEDGE
        acknowledgementMode: "AUTO_ACKNOWLEDGE"
    });

// Initialize a Queue consumer using the created session.
listener jms:DurableTopicListener topicSubscriber = new(jmsSession, "testTopicSubscriberPublisher", "sub-id-1");

// Bind the created consumer to the listener service.
service jmsListener on topicSubscriber {

    // OnMessage resource get invoked when a message is received.
    resource function onMessage(jms:DurableTopicSubscriberCaller consumer, jms:Message message) {

        var messageContent = message.getTextMessageContent();
        if (messageContent is string) {
             msgVal = <@untainted> messageContent;
        } else {
             panic messageContent;
        }
    }
}

function getMsgVal() returns string {
    return msgVal;
}
