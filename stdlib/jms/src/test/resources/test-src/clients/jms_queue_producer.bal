import ballerinax/java.jms;

jms:Connection jmsConnection = new ({
        initialContextFactory: "org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory",
        providerUrl: "tcp://localhost:61616"
    });

jms:Session jmsSession = new (jmsConnection, {
        acknowledgementMode: "AUTO_ACKNOWLEDGE"
    });

jms:QueueSender queueSender = new(jmsSession, "MyQueue4");

public function sendTextMessage () {
    jms:Message msg = checkpanic new jms:Message(jmsSession, jms:TEXT_MESSAGE);
    checkpanic msg.setPayload("Test Text");
    // Send the Ballerina message to the JMS provider.
    checkpanic queueSender->send(msg);
}
