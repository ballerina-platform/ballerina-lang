import ballerina/jms;
import ballerina/io;
import ballerina/http;

listener jms:QueueReceiver queueConsumer5 = new({
    initialContextFactory: "bmbInitialContextFactory",
    providerUrl: "amqp://admin:admin@carbon/carbon?brokerlist='tcp://localhost:5772'"
}, queueName = "testMbSimpleQueueReceiverProducer");

// Bind the created consumer to the listener service.
service jmsListener5 on queueConsumer5 {

    // OnMessage resource get invoked when a message is received.
    resource function onMessage(jms:QueueReceiverCaller consumer, jms:Message message) {
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
listener  http:Listener helloWorldEp5 = new(9095);

@http:ServiceConfig {
    basePath:"/jmsDummyService"
}
service helloWorld5 on helloWorldEp5 {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/"
    }
    resource function sayHello (http:Caller caller, http:Request req) {
        // Do nothing
    }
}
