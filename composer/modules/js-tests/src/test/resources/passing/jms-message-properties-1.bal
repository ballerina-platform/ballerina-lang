import ballerina.net.jms;

@jms:configuration {
    initialContextFactory:"wso2mbInitialContextFactory",
    providerUrl:
    "amqp://admin:admin@carbon/carbon?brokerlist='tcp://localhost:5672'",
    connectionFactoryType:"queue",
    connectionFactoryName:"QueueConnectionFactory",
    destination:"MyQueue"
}
service<jms> jmsService {
    resource onMessage (jms:JMSMessage m) {
        endpoint<jms:JmsClient> jmsEP {
             create jms:JmsClient (getConnectorConfig());
        }

        // Get and Print message properties values.
        // Ballerina Supports JMS property types of string, boolean, float and int
        println("String Property : " + m.getStringProperty("string-prop"));
        println("Boolean Property : " + m.getBooleanProperty("boolean-prop"));
        println("----------------------------------");

        // Create an empty Ballerina message.
        jms:JMSMessage responseMessage = jms:createTextMessage(getConnectorConfig());
        // Set a string payload to the message.
        responseMessage.setTextMessageContent("Hello from Ballerina!");

        responseMessage.setIntProperty("int-prop",777);
        responseMessage.setFloatProperty("float-prop",123);

        jmsEP.send("MySecondQueue", responseMessage);
    }
}

function getConnectorConfig () (jms:ClientProperties) {
    jms:ClientProperties properties = {
                                         initialContextFactory:"wso2mbInitialContextFactory",
                                         configFilePath:"../jndi.properties",
                                         connectionFactoryName: "QueueConnectionFactory",
                                         connectionFactoryType : "queue"
                                     };
    return properties;
}

