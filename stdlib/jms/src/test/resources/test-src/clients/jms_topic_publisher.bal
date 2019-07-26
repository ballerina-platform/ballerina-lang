import ballerinax/java.jms;

jms:Connection jmsConnection = new ({
        initialContextFactory: "org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory",
        providerUrl: "tcp://localhost:61616"
    });

jms:Session jmsSession = new (jmsConnection, {
        acknowledgementMode: "AUTO_ACKNOWLEDGE"
    });

jms:TopicPublisher publisher = new(jmsSession, "testDurableTopicSubscriberPublisher6");

public function sendTextMessage () {
    var msg = checkpanic new jms:Message(jmsSession, jms:TEXT_MESSAGE);
    checkpanic msg.setPayload("Test Text");
    checkpanic publisher->send(msg);
}
