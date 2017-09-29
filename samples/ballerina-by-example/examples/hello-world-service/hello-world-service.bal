import ballerina.net.http;
import ballerina.doc;
import ballerina.net.http.response;

@doc:Description {value:"By default Ballerina assumes that the service is to be exposed via HTTP/1.1 using the system default port and that all requests coming to the HTTP server will be delivered to this service."}
service<http> helloWorld {
    @doc:Description {value:"All resources are invoked with an argument of type message, the built-in reference type representing a network invocation."}
    resource sayHello (http:Request req, http:Response res) {
        // A util method that can be used to set string payload.
        response:setStringPayload(res, "Hello, World!");
        // Sends the response back to the client.
        response:send(res);
    }
}
