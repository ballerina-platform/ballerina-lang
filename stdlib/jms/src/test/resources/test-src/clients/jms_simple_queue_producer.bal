import ballerinax/jms;

// Create a queue sender
jms:QueueSender queueSender = new({
    initialContextFactory: "org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory",
    providerUrl: "tcp://localhost:61616"
    }, queueName = "testMbSimpleQueueReceiverProducer");

public function sendTextMessage () {
    // Create a Text message.
    var msg = queueSender.session.createTextMessage("Test Text");
    if (msg is jms:Message) {
         // Send the Ballerina message to the JMS provider.
         checkpanic queueSender->send(msg);
    } else {
         panic msg;
    }
}
