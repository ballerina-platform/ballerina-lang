import ballerina.io;
import ballerina.net.http;

@http:configuration {
    basePath:"/hello"
}
service<http> helloWorld {

    @http:resourceConfig {
        path:"/"
    }
    resource sayHello (http:Connection conn, http:Request req) {
        // Check if the client expects a 100-continue response.
        if (req.expects100Continue()) {
            // Send a 100-continue response to the client.
            _ = conn.respondContinue();
        }

        // The client will start sending the payload once it receives the 100-continue response. Get this payload sent by the client.
        var payload, payloadError = req.getStringPayload();
        http:Response res = {};
        if (payloadError == null) {
            // Process the payload
            io:println(payload);
            // Prepare and send the final response.
            res.statusCode = 200;
            res.setStringPayload("Hello World!\n");
        } else {
            res.statusCode = 500;
            res.setStringPayload(payloadError.message);
        }
        _ = conn.respond(res);
    }
}