import ballerina.net.jms;

@jms:configuration {
    initialContextFactory:"wso2mbInitialContextFactory",
    providerUrl:
    "amqp://admin:admin@carbon/carbon?brokerlist='tcp://localhost:5672'",
    connectionFactoryName:"QueueConnectionFactory",
    destination:"MyQueue",
    acknowledgementMode:jms:CLIENT_ACKNOWLEDGE
}
service<jms> jmsService {
    resource onMessage (jms:JMSMessage request) {
        endpoint<jms:JmsClient> jmsEP {
             create jms:JmsClient (getConnectorConfig());
        }

        //Process the message
        println("Payload: " + request.getTextMessageContent());

        jms:JMSMessage message2 = jms:createTextMessage(getConnectorConfig());
        message2.setTextMessageContent("{\"WSO2\":\"Ballerina\"}");
        transaction {
            jmsEP.send("MyQueue2", message2);
            jmsEP.send("MyQueue3", message2);
        } failed {
            println("Reliable delivery process failed and rollbacked");
        } committed {
            println("Reliable delivery process successed and committed");
        }
    }
}

function getConnectorConfig () (jms:ClientProperties) {
    jms:ClientProperties properties = {
                                          initialContextFactory:"wso2mbInitialContextFactory",
                                          configFilePath:"../jndi.properties",
                                          connectionFactoryName: "QueueConnectionFactory",
                                          connectionFactoryType : "queue",
                                          acknowledgementMode: "SESSION_TRANSACTED"
                                      };
    return properties;
}


