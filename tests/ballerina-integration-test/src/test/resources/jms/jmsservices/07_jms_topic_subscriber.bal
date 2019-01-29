import ballerina/jms;
import ballerina/io;
import ballerina/http;


// Initialize a JMS connection with the provider.
jms:Connection conn6 = new ({
        initialContextFactory: "bmbInitialContextFactory",
        providerUrl: "amqp://admin:admin@carbon/carbon?brokerlist='tcp://localhost:5772'"
    });

// Initialize a JMS session on top of the created connection.
jms:Session jmsSession6 = new (conn6, {
        // Optional property. Defaults to AUTO_ACKNOWLEDGE
        acknowledgementMode: "AUTO_ACKNOWLEDGE"
    });

// Initialize a Queue consumer using the created session.
listener jms:TopicSubscriber topicSubscriber6 = new(jmsSession6, topicPattern = "testDurableTopicSubscriberPublisher6");

// Bind the created consumer to the listener service.
service jmsListener6 on topicSubscriber6 {

    // OnMessage resource get invoked when a message is received.
    resource function onMessage(jms:TopicSubscriberCaller subscriber, jms:Message message) {
        var messageText = message.getTextMessageContent();
        if (messageText is string) {
             io:println("Message : " + messageText);
        } else {
             panic messageText;
        }
    }
}

// This is to make sure that the test case can detect the PID using port. Removing following will result in
// intergration testframe work failing to kill the ballerina service.
listener http:Listener helloWorldEp6 = new(9096);

@http:ServiceConfig {
    basePath:"/jmsDummyService"
}
service helloWorld6 on helloWorldEp6 {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/"
    }
    resource function sayHello (http:Caller caller, http:Request req) {
        // Do nothing
    }
}
