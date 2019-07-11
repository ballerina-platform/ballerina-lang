import ballerinax/jms;

string msgVal = "";
// Initialize a JMS connection with the provider.
jms:Connection conn2 = new ({
        initialContextFactory: "org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory",
        providerUrl: "tcp://localhost:61616"
    });

// Initialize a JMS session on top of the created connection.
jms:Session jmsSession2 = new (conn2, {
    // Optional property. Defaults to AUTO_ACKNOWLEDGE
    acknowledgementMode: "AUTO_ACKNOWLEDGE"
});

// Initialize a Queue consumer using the created session.
listener jms:TopicListener topicSubscriber2 = new(jmsSession2, topicPattern = "testMapMessageSubscriber");

// Bind the created consumer to the listener service.
service jmsListener2 on topicSubscriber2 {

    // OnMessage resource get invoked when a message is received.
    resource function onMessage(jms:TopicSubscriberCaller subscriber, jms:Message message) {
        var messageRetrieved = <@untainted> message.getMapMessageContent();
        if (messageRetrieved is map<any>) {
             msgVal += messageRetrieved["a"].toString();
             msgVal += messageRetrieved["b"].toString();
             msgVal += messageRetrieved["c"].toString();
             msgVal += messageRetrieved["d"].toString();
             byte[] retrievedBlob = <byte[]>messageRetrieved["e"];
        } else {
             panic messageRetrieved;
        }
    }
}

function getMsgVal() returns string {
    return msgVal;
}
