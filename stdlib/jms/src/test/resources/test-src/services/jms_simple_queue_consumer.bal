import ballerinax/java.jms;

string msgVal = "";
listener jms:QueueListener queueConsumer5 = new({
    initialContextFactory: "org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory",
    providerUrl: "tcp://localhost:61616"
}, "testMbSimpleQueueReceiverProducer");

service jmsListener5 on queueConsumer5 {

    resource function onMessage(jms:QueueReceiverCaller consumer, jms:Message message) {
        var messageText = message.getPayload();
        if (messageText is string) {
             msgVal = <@untainted> messageText;
        }
    }
}

function getMsgVal() returns string {
    return msgVal;
}
