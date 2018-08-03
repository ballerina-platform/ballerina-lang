import ballerina/mb;
import ballerina/io;
import ballerina/http;

endpoint mb:SimpleQueueReceiver queueConsumer7 {
    host: "localhost",
    port: 5772,
    queueName: "testMbSimpleQueueReceiverProducer"
};

// Bind the created consumer to the listener service.
service<mb:Consumer> jmsListener7 bind queueConsumer7 {

    // OnMessage resource get invoked when a message is received.
    onMessage(endpoint consumer, mb:Message message) {
        string messageText = check message.getTextMessageContent();
        io:println("Message : " + messageText);
    }
}

// This is to make sure that the test case can detect the PID using port. Removing following will result in
// intergration testframe work failing to kill the ballerina service.
endpoint http:Listener helloWorldEp7 {
    port:9097
};

@http:ServiceConfig {
    basePath:"/jmsDummyService"
}
service<http:Service> helloWorld7 bind helloWorldEp7 {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/"
    }
    sayHello (endpoint client, http:Request req) {
        // Do nothing
    }
}
