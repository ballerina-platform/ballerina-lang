import ballerinax/java.jms;

string msgVal = "";
jms:Connection conn3 = new ({
        initialContextFactory: "org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory",
        providerUrl: "tcp://localhost:61616"
    });

jms:Session jmsSession3 = new (conn3, {
        acknowledgementMode: "AUTO_ACKNOWLEDGE"
    });

listener jms:QueueListener queueConsumer3 = new(jmsSession3, "MyPropQueue");

service jmsListener3 on queueConsumer3 {

    resource function onMessage(jms:QueueReceiverCaller consumer, jms:Message message) {
        var messageText = message.getPayload();
        var booleanVal = message.getProperty("booleanProp");
        if (booleanVal is boolean) {
             msgVal += "booleanVal:" + <@untainted> booleanVal.toString();
        }
        var intVal = message.getProperty("intProp");
        if (intVal is int) {
             msgVal += "|intVal:" + <@untainted> intVal.toString();
        }
        var floatVal = message.getProperty("floatProp");
        if (floatVal is float) {
             msgVal += "|floatVal:" + <@untainted> floatVal.toString();
        }
        var stringProp = message.getProperty("stringProp");
        if (stringProp is string) {
             msgVal += "|stringVal:" + <@untainted> stringProp;
        }
        if (messageText is string) {
             msgVal += "|message:" + <@untainted> messageText;
        }
    }
}

function getMsgVal() returns string {
    return msgVal;
}
