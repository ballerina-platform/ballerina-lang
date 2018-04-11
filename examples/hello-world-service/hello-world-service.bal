import ballerina/http;

@Description {value:"Attributes associated with the service endpoint is defined here."}
endpoint http:Listener helloWorldEP {
    port:9090
};

@Description {value:"By default Ballerina assumes that the service is to be exposed via HTTP/1.1."}
service<http:Service> hello bind helloWorldEP {
    @Description {value:"All resources are invoked with arguments of server connector and request"}
    sayHello (endpoint conn, http:Request req) {
        http:Response res = new;
        // A util method that can be used to set string payload.
        res.setStringPayload("Hello, World!");
        // Sends the response back to the client.
        _ = conn -> respond(res);
    }
}
