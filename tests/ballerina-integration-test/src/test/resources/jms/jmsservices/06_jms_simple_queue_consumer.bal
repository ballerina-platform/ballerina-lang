import ballerina/jms;
import ballerina/io;
import ballerina/http;

endpoint jms:SimpleQueueReceiver queueConsumer5 {
    initialContextFactory: "bmbInitialContextFactory",
    providerUrl: "amqp://admin:admin@carbon/carbon?brokerlist='tcp://localhost:5772'",
    queueName: "testMbSimpleQueueReceiverProducer"
};

// Bind the created consumer to the listener service.
service<jms:Consumer> jmsListener5 bind queueConsumer5 {

    // OnMessage resource get invoked when a message is received.
    onMessage(endpoint consumer, jms:Message message) {
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
endpoint http:Listener helloWorldEp5 {
    port:9095
};

@http:ServiceConfig {
    basePath:"/jmsDummyService"
}
service<http:Service> helloWorld5 bind helloWorldEp5 {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/"
    }
    sayHello (endpoint client, http:Request req) {
        // Do nothing
    }
}
