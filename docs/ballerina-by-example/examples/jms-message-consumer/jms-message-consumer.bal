import ballerina/jms;
import ballerina/log;

jms:Connection conn = new ({
    initialContextFactory: "wso2mbInitialContextFactory",
    providerUrl: "amqp://admin:admin@carbon/carbon?brokerlist='tcp://localhost:5672'",
    connectionFactoryName: "ConnectionFactory"
});

jms:Session jmsSession = new (conn, {
    acknowledgementMode: "CLIENT_ACKNOWLEDGE"
});

endpoint jms:QueueConsumer consumer {
    session: jmsSession,
    queueName: "requestQueue"
};

service<jms:Consumer> jmsListener bind consumer {

    onMessage(endpoint consumer, jms:Message message) {
        string messageText = message.getTextMessageContent();
        log:printInfo("Message : " + messageText);
        // consumer -> acknowledge (message);
  }
}
