import ballerina.net.http;

@Description {value:"By default Ballerina assumes that the service is to be exposed via HTTP/1.1 using the system default port."}
service<http> helloWorld {
    @Description {value:"All resources are invoked with arguments of request and response"}
    resource sayHello (http:Request req, http:Response res) {
        // A util method that can be used to set string payload.
        res.setStringPayload("Hello, World!");
        // Sends the response back to the client.
        res.send();
    }
}
