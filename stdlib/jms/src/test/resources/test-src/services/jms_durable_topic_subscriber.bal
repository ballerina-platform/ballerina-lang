import ballerinax/java.jms;

string msgVal = "";
jms:Connection conn = new ({
        initialContextFactory: "org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory",
        providerUrl: "tcp://localhost:61616"
    });

jms:Session jmsSession = new (conn, {
        // Optional property. Defaults to AUTO_ACKNOWLEDGE
        acknowledgementMode: "AUTO_ACKNOWLEDGE"
    });

listener jms:DurableTopicListener topicSubscriber = new(jmsSession, "testTopicSubscriberPublisher", "sub-id-1");

service jmsListener on topicSubscriber {

    resource function onMessage(jms:DurableTopicSubscriberCaller consumer, jms:Message message) {

        var messageContent = message.getPayload();
        if (messageContent is string) {
             msgVal = <@untainted> messageContent;
        }
    }
}

function getMsgVal() returns string {
    return msgVal;
}
