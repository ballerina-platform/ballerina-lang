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
endpoint jms:QueueReceiver consumer {
    session: jmsSession,
    queueName: "MyQueue"
};

// Bind the created consumer to the listener service.
service<jms:Consumer> jmsListener bind consumer {

    // OnMessage resource get invoked when a message is received.
    onMessage(endpoint consumer, jms:Message message) {
        string messageText = check message.getTextMessageContent();
        string correlationId = check message.getCorrelationID();
        io:print("correlationId:" + correlationId);
        int intVal = check message.getIntProperty("intProp");
        io:print("|intVal:" + intVal);
        float floatVal = check message.getFloatProperty("floatProp");
        io:print("|floatVal:" + floatVal);

        match (check message.getStringProperty("stringProp")) {
            string s => io:print("|stringVal:" + s);
            () => io:print("error");
        }
        io:println("|message:" + messageText);
    }
}

// This is to make sure that the test case can detect the PID using port. Removing following will result in
// intergration testframe work failing to kill the ballerina service.
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
