import ballerina/jms;
import ballerina/io;
import ballerina/http;


// Initialize a JMS connection with the provider.
jms:Connection conn2 = new ({
        initialContextFactory: "bmbInitialContextFactory",
        providerUrl: "amqp://admin:admin@carbon/carbon?brokerlist='tcp://localhost:5772'"
    });

// Initialize a JMS session on top of the created connection.
jms:Session jmsSession2 = new (conn2, {
    // Optional property. Defaults to AUTO_ACKNOWLEDGE
    acknowledgementMode: "AUTO_ACKNOWLEDGE"
});

// Initialize a Queue consumer using the created session.
listener jms:TopicSubscriber topicSubscriber2 = new(jmsSession2, topicPattern = "testMapMessageSubscriber");

// Bind the created consumer to the listener service.
service jmsListener2 on topicSubscriber2 {

    // OnMessage resource get invoked when a message is received.
    resource function onMessage(jms:TopicSubscriberCaller subscriber, jms:Message message) {
        var messageRetrieved = message.getMapMessageContent();
        if (messageRetrieved is map<any>) {
             io:print(messageRetrieved["a"]);
             io:print(messageRetrieved["b"]);
             io:print(messageRetrieved["c"]);
             io:println(messageRetrieved["d"]);
             byte[] retrievedBlob = <byte[]>messageRetrieved["e"];
        } else {
             panic messageRetrieved;
        }
    }
}

// This is to make sure that the test case can detect the PID using port. Removing following will result in
// intergration testframe work failing to kill the ballerina service.
listener http:Listener helloWorldEp2 = new(9092);

@http:ServiceConfig {
    basePath:"/jmsDummyService"
}
service helloWorld2 on helloWorldEp2 {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/"
    }
    resource function sayHello (http:Caller caller, http:Request req) {
        // Do nothing
    }
}
