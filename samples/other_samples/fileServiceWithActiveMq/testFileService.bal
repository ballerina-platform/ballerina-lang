import ballerina.lang.messages;
import ballerina.lang.system;
import ballerina.net.file;
import ballerina.net.jms;

@Source (
protocol = "file",
fileURI = "ftp://username:password@localhost:2221/orders",
pollingInterval = "10000"
)
service testFileService {

    resource processOrder(message m) {
        map dataMap = {};
        system:println(messages:getStringPayload(m));
        jms:ClientConnector jmsEP = create jms:ClientConnector("org.apache.activemq.jndi.ActiveMQInitialContextFactory", "tcp://localhost:61616");
        jms:ClientConnector.send(jmsEP, "QueueConnectionFactory", "order", "queue", "TextMessage", m, dataMap);
        file:acknowledge(m);
    }
}
