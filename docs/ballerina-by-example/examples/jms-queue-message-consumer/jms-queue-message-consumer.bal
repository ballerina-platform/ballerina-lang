import ballerina/jms;
import ballerina/log;

jms:Connection conn = new ({
    initialContextFactory: "wso2mbInitialContextFactory",
    providerUrl: "amqp://admin:admin@carbon/carbon?brokerlist='tcp://localhost:5672'",
    connectionFactoryName: "ConnectionFactory"
});

jms:Session jmsSession = new (conn, {
    acknowledgementMode: "AUTO_ACKNOWLEDGE"
});

endpoint jms:QueueConsumer consumer {
    session: jmsSession,
    queueName: "MyQueue"
};

service<jms:Consumer> jmsListener bind consumer {

    onMessage(endpoint consumer, jms:Message message) {
        string messageText = message.getTextMessageContent();
        log:printInfo("Message : " + messageText);
  }
}
