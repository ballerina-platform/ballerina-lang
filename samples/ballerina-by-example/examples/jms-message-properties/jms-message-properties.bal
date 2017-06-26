import ballerina.lang.messages;
import ballerina.lang.system;
import ballerina.net.jms;

@jms:JMSSource {
    factoryInitial:"org.wso2.andes.jndi.PropertiesFileInitialContextFactory",
    providerUrl:"../jndi.properties"}
@jms:ConnectionProperty {key:"connectionFactoryType", value:"queue"}
@jms:ConnectionProperty {key:"destination", value:"MyQueue"}
@jms:ConnectionProperty {key:"useReceiver", value:"true"}
@jms:ConnectionProperty {key:"connectionFactoryJNDIName", 
                        value:"QueueConnectionFactory"}
@jms:ConnectionProperty {key:"sessionAcknowledgement", value:"AUTO_ACKNOWLEDGE"}
service jmsService {
    resource onMessage (message m) {
        // Read a message property.
        string myProperty = messages:getProperty(m, "MyProperty");
        // Print the property values
        system:println("myProperty value " + myProperty);

        message responseMessage = {};
        // Set a custom message property. This value will be treated as a JMS 
        // message string property when sending to a JMS provider
        messages:setProperty(responseMessage, "MySecondProperty", 
                                              "Hello from Ballerina!");

        map properties = {  
                "factoryInitial":
                    "org.wso2.andes.jndi.PropertiesFileInitialContextFactory",
                "providerUrl":"../jndi.properties",
                "connectionFactoryJNDIName": "QueueConnectionFactory",
                "connectionFactoryType" : "queue"};

        jms:ClientConnector jmsEP = create jms:ClientConnector(properties);
        jms:ClientConnector.send(jmsEP, "MySecondQueue", responseMessage);

    }
}
