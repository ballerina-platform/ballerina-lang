import ballerina.net.jms;
import ballerina.lang.messages;

function main(string[] args) {
    jmsSender();

}

function jmsSender() (boolean) {
	map properties = {	"factoryInitial":"org.apache.activemq.jndi.ActiveMQInitialContextFactory",
						"providerUrl":"tcp://localhost:61616",
						"connectionFactoryJNDIName": "QueueConnectionFactory",
						"connectionFactoryType" : "queue"};

    jms:ClientConnector jmsEP = create jms:ClientConnector(properties);
    message queueMessage = {};
    messages:setStringPayload(queueMessage, "Hello from JMS");

    jms:ClientConnector.send(jmsEP, "MyQueue", "TextMessage", queueMessage);
    return true;
}