import ballerina/jms;
import ballerina/io;
import ballerina/http;


// Initialize a JMS connection with the provider.
jms:Connection conn = new ({
        initialContextFactory: "bmbInitialContextFactory",
        providerUrl: "amqp://admin:admin@carbon/carbon?brokerlist='tcp://localhost:5772'"
    });

// Initialize a JMS session on top of the created connection.
jms:Session jmsSession = new (conn, {
        // Optional property. Defaults to AUTO_ACKNOWLEDGE
        acknowledgementMode: "AUTO_ACKNOWLEDGE"
    });

// Initialize a Queue consumer using the created session.
listener jms:DurableTopicSubscriber topicSubscriber = new(jmsSession, "testTopicSubscriberPublisher", "sub-id-1");

// Bind the created consumer to the listener service.
service jmsListener on topicSubscriber {

    // OnMessage resource get invoked when a message is received.
    resource function onMessage(jms:DurableTopicSubscriberCaller consumer, jms:Message message) {
        var messageContent = message.getTextMessageContent();
        if (messageContent is string) {
             io:println("Message : " + messageContent);
        } else {
             panic messageContent;
        }
    }
}

// This is to make sure that the test case can detect the PID using port. Removing following will result in
// intergration testframe work failing to kill the ballerina service.
listener http:Listener helloWorldEp = new(9090);

@http:ServiceConfig {
    basePath:"/jmsDummyService"
}
service helloWorld on helloWorldEp {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/"
    }
    resource function sayHello (http:Caller caller, http:Request req) {
        // Do nothing
    }
}
