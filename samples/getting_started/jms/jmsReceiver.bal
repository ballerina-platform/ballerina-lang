import ballerina.lang.messages;
import ballerina.lang.system;
import ballerina.net.jms;

@jms:JMSSource {
factoryInitial : "org.apache.activemq.jndi.ActiveMQInitialContextFactory",
providerUrl : "tcp://localhost:61616"}
@jms:ConnectionProperty{key:"connectionFactoryType", value:"queue"}
@jms:ConnectionProperty{key:"destination", value:"MyQueue"}
@jms:ConnectionProperty{key:"useReceiver", value:"true"}
@jms:ConnectionProperty{key:"connectionFactoryJNDIName", value:"QueueConnectionFactory"}
@jms:ConnectionProperty{key:"sessionAcknowledgement", value:"AUTO_ACKNOWLEDGE"}
service jmsService {
    resource onMessage (message m) {
        //Process the message
        string msgType = messages:getProperty(m, "JMS_MESSAGE_TYPE");
        string stringPayload = messages:getStringPayload(m);
        system:println("message type : " + msgType);
        system:println(stringPayload);
    }
}