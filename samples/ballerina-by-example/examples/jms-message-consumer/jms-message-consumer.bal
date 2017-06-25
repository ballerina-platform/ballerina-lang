import ballerina.lang.messages;
import ballerina.lang.system;
import ballerina.net.jms;
import ballerina.doc;

@doc:Description{value : "Service level annotation to provide connection details"}
@jms:JMSSource {
    factoryInitial:"org.wso2.andes.jndi.PropertiesFileInitialContextFactory",
    providerUrl:"../jndi.properties"}
@doc:Description{value : "Connect to a queue. We can use 'topic' when connecting to a topic"}
@jms:ConnectionProperty {key:"connectionFactoryType", value:"queue"}
@doc:Description{value : "Connect to a queue. We can use 'topic' when connecting to a topic"}
@jms:ConnectionProperty {key:"destination", value:"MyQueue"}
@jms:ConnectionProperty {key:"useReceiver", value:"true"}
@jms:ConnectionProperty {key:"connectionFactoryJNDIName", 
                         value:"QueueConnectionFactory"}
@doc:Description{value : "JMS acknowledgment mode for the subscriber"}
@jms:ConnectionProperty {key:"sessionAcknowledgement", value:"AUTO_ACKNOWLEDGE"}
service jmsService {
    resource onMessage (message m) {

        // Retrieve the string payload using native function
        string stringPayload = messages:getStringPayload(m);

        // Print the retrieved payload
        system:println("Payload: " + stringPayload);
    }
}
