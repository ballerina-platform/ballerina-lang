import ballerina/io;
import ballerina/http;

@Description {value:"Attributes associated with the service endpoint is defined here."}
endpoint http:ServiceEndpoint helloWorldEP {
    port:9090
};

@http:ServiceConfig {
          basePath: "/hello"
}
service<http:Service> helloWorld bind helloWorldEP{

    @http:ResourceConfig {
        path:"/"
    }
    hello (endpoint outboundEP, http:Request request) {
        // Check if the client expects a 100-continue response.
        if (request.expects100Continue()) {
            // Send a 100-continue response to the client.
            _ = outboundEP -> respondContinue();
        }

        // The client will start sending the payload once it receives the 100-continue response. Get this payload sent by the client.
        http:Response res = new;
        match request.getStringPayload() {
            string payload => {
                io:println(payload);
                res.statusCode =200;
                res.setStringPayload("Hello World!\n");
                _ = outboundEP -> respond(res);
            }
            error payloadError =>  {
                res.statusCode = 500;
                res.setStringPayload(payloadError.message);
                _ = outboundEP -> respond(res);
            }
        }
    }
}