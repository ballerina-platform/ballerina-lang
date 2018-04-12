import ballerina/mb;
import ballerina/io;
import ballerina/http;

endpoint mb:SimpleQueueReceiver consumer {
    host: "localhost",
    port: 5772,
    queueName: "testMbSimpleQueueReceiverProducer"
};

// Bind the created consumer to the listener service.
service<mb:Consumer> jmsListener bind consumer {

    // OnMessage resource get invoked when a message is received.
    onMessage(endpoint consumer, mb:Message message) {
        string messageText = check message.getTextMessageContent();
        io:println("Message : " + messageText);
    }
}

// This is to make sure that the test case can detect the PID using port.
endpoint http:Listener helloWorldEp {
    port:9090
};

@http:ServiceConfig {
    basePath:"/jmsDummyService"
}
service<http:Service> helloWorld bind helloWorldEp {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/"
    }
    sayHello (endpoint client, http:Request req) {
        // Do nothing
    }
}
