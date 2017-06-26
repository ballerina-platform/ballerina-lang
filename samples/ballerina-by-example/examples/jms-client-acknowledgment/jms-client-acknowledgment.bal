import ballerina.lang.messages;
import ballerina.lang.system;
import ballerina.net.jms;
import ballerina.doc;

@jms:JMSSource {
    factoryInitial:"org.wso2.andes.jndi.PropertiesFileInitialContextFactory",
    providerUrl:"../jndi.properties"}
@jms:ConnectionProperty {key:"connectionFactoryType", value:"queue"}
@jms:ConnectionProperty {key:"destination", value:"MyQueue"}
@jms:ConnectionProperty {key:"useReceiver", value:"true"}
@jms:ConnectionProperty {key:"connectionFactoryJNDIName", 
                         value:"QueueConnectionFactory"}
@doc:Description{ value:"Set the acknowledgment mode to 'CLIENT_ACKNOWLEDGME'"}
@jms:ConnectionProperty {key:"sessionAcknowledgement", 
					     value:"CLIENT_ACKNOWLEDGE"}
service jmsService {
    resource onMessage (message m) {

        string stringPayload = messages:getStringPayload(m);
        system:println("Payload: " + stringPayload);
        // acknowledge the message with positive acknowledgment. If we want to reject the message due to some error
        // we can use the same method with second argument as 'jms:DELIVERY_ERROR'
        jms:acknowledge(m, jms:DELIVERY_SUCCESS);
        
    }
}
