import ballerinax/jms;

string msgVal = "";
listener jms:QueueListener queueConsumer5 = new({
    initialContextFactory: "org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory",
    providerUrl: "tcp://localhost:61616"
}, "testMbSimpleQueueReceiverProducer");

// Bind the created consumer to the listener service.
service jmsListener5 on queueConsumer5 {

    // OnMessage resource get invoked when a message is received.
    resource function onMessage(jms:QueueReceiverCaller consumer, jms:Message message) {
        var messageText = message.getTextMessageContent();
        if (messageText is string) {
             msgVal = <@untainted> messageText;
        } else {
             panic messageText;
        }
    }
}

function getMsgVal() returns string {
    return msgVal;
}
