import ballerinax/java.jms;

jms:QueueSender queueSender = new({
    initialContextFactory: "org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory",
    providerUrl: "tcp://localhost:61616"
    }, "testMbSimpleQueueReceiverProducer");

public function sendTextMessage () {
    jms:Message msg = checkpanic new jms:Message(queueSender.session, jms:TEXT_MESSAGE);
    checkpanic msg.setPayload("Test Text");
    checkpanic queueSender->send(msg);
}
