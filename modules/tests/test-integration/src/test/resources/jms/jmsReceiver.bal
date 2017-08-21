import ballerina.lang.messages;
import ballerina.lang.system;
import ballerina.net.jms;
import ballerina.net.http;

@jms:configuration {
    initialContextFactory:"org.apache.activemq.jndi.ActiveMQInitialContextFactory",
    providerUrl:"tcp://localhost:61616",
    connectionFactoryType:"queue",
    connectionFactoryName:"QueueConnectionFactory",
    destination:"MyQueue",
    acknowledgmentMode:"AUTO_ACKNOWLEDGE"
}
service<jms> jmsService {
    resource onMessage (message m) {
        //Process the message
        string msgType = messages:getProperty(m, "JMS_MESSAGE_TYPE");
        string stringPayload = messages:getStringPayload(m);
        system:println("message type : " + msgType);
        system:println(stringPayload);
    }
}



@http:configuration {
    basePath:"/echo"
}
service<http> echo {

    @http:resourceConfig {
        methods:["POST"],
        path:"/"
    }
    resource echo (message m) {
        message resp = {};
        messages:setStringPayload(resp, "hello world");
        reply resp;
    }
}
