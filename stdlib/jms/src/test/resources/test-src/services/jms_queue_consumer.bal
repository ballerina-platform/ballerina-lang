import ballerinax/java.jms;


string msgVal = "";
jms:Connection conn4 = new ({
        initialContextFactory: "org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory",
        providerUrl: "tcp://localhost:61616"
    });

jms:Session jmsSession4 = new (conn4, {
        // Optional property. Defaults to AUTO_ACKNOWLEDGE
        acknowledgementMode: "AUTO_ACKNOWLEDGE"
    });

listener jms:QueueListener queueConsumer4 = new(jmsSession4, "MyQueue4");

service jmsListener4 on queueConsumer4 {

    resource function onMessage(jms:QueueReceiverCaller consumer , jms:Message message) {
        var messageText = message.getPayload();
        if (messageText is string) {
             msgVal = <@untainted> messageText;
        }
    }
}

function getMsgVal() returns string {
    return msgVal;
}
