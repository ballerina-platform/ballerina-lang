import ballerinax/jms;

string msgVal = "";
// Initialize a JMS connection with the provider.
jms:Connection conn3 = new ({
        initialContextFactory: "org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory",
        providerUrl: "tcp://localhost:61616"
    });

// Initialize a JMS session on top of the created connection.
jms:Session jmsSession3 = new (conn3, {
        // Optional property. Defaults to AUTO_ACKNOWLEDGE
        acknowledgementMode: "AUTO_ACKNOWLEDGE"
    });

// Initialize a Queue consumer using the created session.
listener jms:QueueListener queueConsumer3 = new(jmsSession3, "MyPropQueue");

// Bind the created consumer to the listener service.
service jmsListener3 on queueConsumer3 {

    // OnMessage resource get invoked when a message is received.
    resource function onMessage(jms:QueueReceiverCaller consumer, jms:Message message) {
        var messageText = message.getTextMessageContent();
        var booleanVal = message.getBooleanProperty("booleanProp");
        if (booleanVal is boolean) {
             msgVal += "booleanVal:" + <@untainted> booleanVal;
        } else {
             panic booleanVal;
        }
        var intVal = message.getIntProperty("intProp");
        if (intVal is int) {
             msgVal += "|intVal:" + <@untainted> intVal;
        } else {
             panic intVal;
        }
        var floatVal = message.getFloatProperty("floatProp");
        if (floatVal is float) {
             msgVal += "|floatVal:" + <@untainted> floatVal;
        } else {
             panic floatVal;
        }
        var stringProp = message.getStringProperty("stringProp");
        if (stringProp is string) {
             msgVal += "|stringVal:" + <@untainted> stringProp;
        } else if (stringProp is error) {
             panic stringProp;
        }
        if (messageText is string) {
             msgVal += "|message:" + <@untainted> messageText;
        } else {
             panic messageText;
        }
    }
}

function getMsgVal() returns string {
    return msgVal;
}
