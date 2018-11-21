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
endpoint jms:TopicSubscriber topicSubscriber2 {
    session: jmsSession2,
    topicPattern: "testMapMessageSubscriber"
};

// Bind the created consumer to the listener service.
service<jms:Consumer> jmsListener2 bind topicSubscriber2 {

    // OnMessage resource get invoked when a message is received.
    onMessage(endpoint subscriber, jms:Message message) {
        var messageRetrieved = message.getMapMessageContent();
        if (messageRetrieved is map) {
             io:print(messageRetrieved["a"]);
             io:print(messageRetrieved["b"]);
             io:print(messageRetrieved["c"]);
             io:println(messageRetrieved["d"]);
             var retrievedBlob = <byte[]>messageRetrieved["e"];
             if (retrievedBlob is error) {
                  panic retrievedBlob;
             }
        } else {
             panic messageRetrieved;
        }
    }
}

// This is to make sure that the test case can detect the PID using port. Removing following will result in
// intergration testframe work failing to kill the ballerina service.
endpoint http:Listener helloWorldEp2 {
    port:9092
};

@http:ServiceConfig {
    basePath:"/jmsDummyService"
}
service<http:Service> helloWorld2 bind helloWorldEp2 {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/"
    }
    sayHello (endpoint client, http:Request req) {
        // Do nothing
    }
}
