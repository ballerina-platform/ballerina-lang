import ballerinax/jms;


string msgVal = "";
// Initialize a JMS connection with the provider.
jms:Connection conn4 = new ({
        initialContextFactory: "org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory",
        providerUrl: "tcp://localhost:61616"
    });

// Initialize a JMS session on top of the created connection.
jms:Session jmsSession4 = new (conn4, {
        // Optional property. Defaults to AUTO_ACKNOWLEDGE
        acknowledgementMode: "AUTO_ACKNOWLEDGE"
    });

// Initialize a Queue consumer using the created session.
listener jms:QueueListener queueConsumer4 = new(jmsSession4, queueName = "MyQueue4");

// Bind the created consumer to the listener service.
service jmsListener4 on queueConsumer4 {

    // OnMessage resource get invoked when a message is received.
    resource function onMessage(jms:QueueReceiverCaller consumer , jms:Message message) {
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
