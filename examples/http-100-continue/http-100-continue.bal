import ballerina/io;
import ballerina/http;

@Description {value:"Attributes associated with the service endpoint are defined here."}
endpoint http:Listener helloWorldEP {
    port:9090
};

@http:ServiceConfig {
          basePath: "/hello"
}
service<http:Service> helloWorld bind helloWorldEP{

    @http:ResourceConfig {
        path:"/"
    }
    hello (endpoint caller, http:Request request) {
        // Check if the client expects a 100-continue response.
        if (request.expects100Continue()) {
            // Send a 100-continue response to the client.
            _ = caller -> continue();
        }

        // The client starts sending the payload once it receives the 100-continue response. Retrieve the payload that is sent by the client.
        http:Response res = new;
        match request.getStringPayload() {
            string payload => {
                io:println(payload);
                res.statusCode =200;
                res.setStringPayload("Hello World!\n");
                _ = caller -> respond(res);
            }
            error payloadError =>  {
                res.statusCode = 500;
                res.setStringPayload(payloadError.message);
                _ = caller -> respond(res);
            }
        }
    }
}
