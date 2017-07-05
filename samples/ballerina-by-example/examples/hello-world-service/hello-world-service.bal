import ballerina.lang.messages;
import ballerina.net.http;
import ballerina.doc;

@doc:Description{value : "By default, Ballerina assumes that the service is to be exposed via HTTP/1.1 using the system default port and that all requests coming to the HTTP server are delivered to this service."}
service<http> helloWorld {
    @doc:Description{value :  "All resources are invoked with an argument of type message. This is the built-in reference type representing a network invocation."}
    resource sayHello (message m) {
        // This is how you create an empty message.
        message response = {};
        // This is a util method that can be used to set string payload.
        messages:setStringPayload(response, "Hello, World!");
        // The "reply" keyword sends the response back to the client.
        reply response;
    }
}
