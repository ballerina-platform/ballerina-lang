import ballerinax/java.jms;

jms:Connection jmsConnection = new({
        initialContextFactory: "org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory",
        providerUrl: "tcp://localhost:61616"
    });

jms:Session jmsSession = new(jmsConnection, {
        acknowledgementMode: "AUTO_ACKNOWLEDGE"
    });

jms:TopicPublisher publisher = new(jmsSession, "testMapMessageSubscriber");

public function sendTextMessage() {
    string stringValue = "abcde";
    byte[] blobValue = stringValue.toBytes();
    map<int | float | boolean | string | byte[]> mapContent = { "a": 1, "b": "abc", "c": true, "d": 1.2,
        "e": blobValue };
    jms:Message msg = checkpanic new jms:Message(jmsSession, jms:MAP_MESSAGE);
    checkpanic msg.setPayload(mapContent);
    checkpanic publisher->send(msg);
}
