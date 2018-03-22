import ballerina/lang.messages;
import ballerina/lang.system;
import ballerina/net.jms;

@jms:configuration {
    initialContextFactory:"wso2mbInitialContextFactory",
    providerUrl:
           "amqp://admin:admin@carbon/carbon?brokerlist='tcp://localhost:5672'",
    connectionFactoryType:"queue",
    connectionFactoryName:"QueueConnectionFactory",
    destination:"MyQueue",
    acknowledgmentMode:"AUTO_ACKNOWLEDGE"
}
service<jms> jmsService {
    resource onMessage (message m) {
        // Read all the supported headers from the message.
        string correlationId = messages:getHeader(m, jms:HDR_CORRELATION_ID);
        string timestamp = messages:getHeader(m, jms:HDR_TIMESTAMP);
        string messageType = messages:getHeader(m, jms:HDR_MESSAGE_TYPE);
        string messageId = messages:getHeader(m, jms:HDR_MESSAGE_ID);
        string destination = messages:getHeader(m, jms:HDR_DESTINATION);
        string redelivered = messages:getHeader(m, jms:HDR_REDELIVERED);
        string expirationTime = messages:getHeader(m, jms:HDR_EXPIRATION);
        string priority = messages:getHeader(m, jms:HDR_PRIORITY);
        string deliveryMode = messages:getHeader(m, jms:HDR_DELIVERY_MODE);
        // For delivery mode we can use the built in constant as follows to infer whether the message is a persistent message or not.
        if (deliveryMode == jms:PERSISTENT_DELIVERY_MODE) {
            system:println("delivery mode: persistent");
        } else if (deliveryMode == jms:PERSISTENT_DELIVERY_MODE){
            system:println("delivery mode: non-persistent");
        }
        // Print the header values.
        system:println("correlationId: " + correlationId);
        system:println("timestamp: " + timestamp);
        system:println("message type : " + messageType);
        system:println("message id : " + messageId);
        system:println("destination : " + destination);
        system:println("is redelivered : " + redelivered);
        system:println("expiration time : " + expirationTime);
        system:println("priority : " + priority);
        system:println("----------------------------------");

        message responseMessage = {};
        // ReplyTo header is set with the name of the queue. You can retrieve this value when receiving a message using the getHeader function as well.
        messages:setHeader(responseMessage, jms:HDR_REPLY_TO, "ResponseQueue");
        messages:setHeader(responseMessage, 
                           jms:HDR_CORRELATION_ID, "response-001");
        messages:setHeader(responseMessage, jms:HDR_PRIORITY, "8");
        messages:setHeader(responseMessage, jms:HDR_DELIVERY_MODE, 
                                            jms:NON_PERSISTENT_DELIVERY_MODE);
        // We can explicitly set the message type. If we use functions like setStringPayload, 
        // setJSONPayload and setXMLPayload content type will be set. 
        // And when we send the message the content type will be used as the message type value. 
        // Therefore we might not need to explicitly set this value.
        messages:setHeader(responseMessage, jms:HDR_MESSAGE_TYPE, 
                                            "application/json");

        map properties = {   
                         "initialContextFactory":"wso2mbInitialContextFactory",
                         "configFilePath":"../jndi.properties",
                         "connectionFactoryName": "QueueConnectionFactory",
                         "connectionFactoryType" : "queue"};

        jms:ClientConnector jmsEP = create jms:ClientConnector(properties);
        jmsEP.send("MySecondQueue", responseMessage);

    }
}
