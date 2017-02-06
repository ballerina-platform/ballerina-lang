import ballerina.lang.system;
import ballerina.lang.message;

@Source (
protocol = "jms", Destination = "queue", ConnectionFactoryJNDIName = "QueueConnectionFactory",
FactoryInitial = "org.apache.activemq.jndi.ActiveMQInitialContextFactory", ProviderUrl = "vm://localhost?broker.persistent=false",
ConnectionFactoryType = "queue")
service jmsService {

    @OnMessage
    resource onTextMessage (message m) {
        system:println ("Hello, World!");
        system:println (message:getStringPayload(m));
    }
}

