import ballerinax/java.jms;

string msgVal = "";

jms:Connection conn6 = new ({
        initialContextFactory: "org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory",
        providerUrl: "tcp://localhost:61616"
    });

jms:Session jmsSession6 = new (conn6, {
        acknowledgementMode: "AUTO_ACKNOWLEDGE"
    });

listener jms:TopicListener topicSubscriber6 = new(jmsSession6, "testDurableTopicSubscriberPublisher6");

service jmsListener6 on topicSubscriber6 {

    resource function onMessage(jms:TopicSubscriberCaller subscriber, jms:Message message) {
        var messageText = message.getPayload();
        if (messageText is string) {
             msgVal = <@untainted> messageText;
        }
    }
}

function getMsgVal() returns string {
    return msgVal;
}
