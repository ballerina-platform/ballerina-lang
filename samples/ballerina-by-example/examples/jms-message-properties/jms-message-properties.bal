import ballerina.net.jms.jmsmessage;
import ballerina.lang.system;
import ballerina.net.jms;

@jms:configuration {
    initialContextFactory:"wso2mbInitialContextFactory",
    providerUrl:
    "amqp://admin:admin@carbon/carbon?brokerlist='tcp://localhost:5672'",
    connectionFactoryType:"queue",
    connectionFactoryName:"QueueConnectionFactory",
    destination:"MyQueue"
}
service<jms> jmsService {
    resource onMessage (jms:JMSMessage m) {
        jms:ClientConnector jmsEP;

        // Read all the supported headers from the message.
        string correlationId = jmsmessage:getCorrelationID(m);
        int timestamp = jmsmessage:getTimestamp(m);
        string messageType = jmsmessage:getType(m);
        string messageId = jmsmessage:getMessageID(m);
        boolean redelivered = jmsmessage:getRedelivered(m);
        int expirationTime = jmsmessage:getExpiration(m);
        int priority = jmsmessage:getPriority(m);
        int deliveryMode = jmsmessage:getDeliveryMode(m);

        // Print the header values.
        system:println("correlationId : " + correlationId);
        system:println("timestamp : " + timestamp);
        system:println("message type : " + messageType);
        system:println("message id : " + messageId);
        system:println("is redelivered : " + redelivered);
        system:println("expiration time : " + expirationTime);
        system:println("priority : " + priority);
        system:println("delivery mode : " + deliveryMode);
        system:println("----------------------------------");

        map properties = {
                             "initialContextFactory":"wso2mbInitialContextFactory",
                             "configFilePath":"../jndi.properties",
                             "connectionFactoryName": "QueueConnectionFactory",
                             "connectionFactoryType" : "queue"};

        jmsEP = create jms:ClientConnector(properties);
        jms:JMSMessage responseMessage = jms:createTextMessage(jmsEP);

        jmsmessage:setCorrelationID(responseMessage, "response-001");
        jmsmessage:setPriority(responseMessage, 8);
        jmsmessage:setDeliveryMode(responseMessage, 1);
        jmsmessage:setTextMessageContent(responseMessage, "{\"WSO2\":\"Ballerina\"}");
        jmsmessage:setType(responseMessage, "application/json");

        jmsEP.send("MySecondQueue", responseMessage);
    }
}
