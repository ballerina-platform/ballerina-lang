@jms:JMSSource {
factoryInitial : "org.apache.activemq.jndi.ActiveMQInitialContextFactory",
providerUrl : "tcp://localhost:61616"}
@jms:ConnectionProperty{ value:"queue",key:"connectionFactoryType"}
@jms:ConnectionProperty{ value:"MyQueue",key:"destination"}
@jms:ConnectionProperty{ value:"true",key:"useReceiver"}
@jms:ConnectionProperty{ value:"QueueConnectionFactory",key:"connectionFactoryJNDIName"}
@jms:ConnectionProperty{ value:"AUTO_ACKNOWLEDGE",key:"sessionAcknowledgement"}
service jmsService {
    resource onMessage (message m) {
        //Process the message
        string msgType = messages:getProperty(m, "JMS_MESSAGE_TYPE");
        string stringPayload = messages:getStringPayload(m);
        system:println("message type : " + msgType);
        system:println(stringPayload);
    }
}