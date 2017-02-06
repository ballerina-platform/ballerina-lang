import ballerina.lang.system;
import ballerina.lang.message;
import ballerina.net.jms;

@Source (
protocol = "jms",
Destination = "ballerina",
ConnectionFactoryJNDIName = "QpidConnectionFactory",
FactoryInitial = "org.wso2.andes.jndi.PropertiesFileInitialContextFactory",
ProviderUrl = "jndi.properties",
ConnectionFactoryType = "queue",
SessionAcknowledgement = "CLIENT_ACKNOWLEDGE")
service jmsMBService {
    @OnMessage
    resource onTextMessage (message m) {
        system:println (message:getStringPayload(m));
        jms:acknowledge(m, "SUCCESS");
    }
}

