import ballerina.net.http;

@http:configuration {
    basePath: "/hello"
}
service<http> helloWorld {

    @http:resourceConfig {
        path: "/"
    }
    resource sayHello (http:Request req, http:Response res) {
        // Check if the client expects a 100-continue response.
        if (req.expects100Continue()) {
            // Send a 100-continue response to the client.
            _ = res.send100Continue();
        }

        // The client will start sending the payload once it receives the 100-continue response. Get this payload sent by the client.
        string payload = req.getStringPayload();
        // Process the payload
        println(payload);

        // Prepare and send the final response.
        res.setStatusCode(200);
        res.setStringPayload("Hello World!\n");

        _ = res.send();
    }
}