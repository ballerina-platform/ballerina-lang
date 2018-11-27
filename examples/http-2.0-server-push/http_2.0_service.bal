import ballerina/http;
import ballerina/log;

// Create an endpoint with port 7090 to accept HTTP requests.
endpoint http:Listener http2ServiceEP {
    port: 7090,
    // HTTP version is set to 2.0.
    httpVersion: "2.0"

};

@http:ServiceConfig {
    basePath: "/http2Service"
}
service http2Service bind http2ServiceEP {

    @http:ResourceConfig {
        path: "/"
    }
    http2Resource(endpoint caller, http:Request req) {

        // Send a Push Promise.
        http:PushPromise promise1 = new(path = "/resource1", method = "GET");
        var result = caller->promise(promise1);

        if (result is error){
           log:printError("Error occurred while sending the promise1",err = result);
        }

        // Send another Push Promise.
        http:PushPromise promise2 = new(path = "/resource2", method = "GET");
        var result = caller->promise(promise2);

        if (result is error){
           log:printError("Error occurred while sending the promise2",err = result);
        }

        // Send one more Push Promise.
        http:PushPromise promise3 = new(path = "/resource3", method = "GET");
        var result = caller->promise(promise3);

        if (result is error){
           log:printError("Error occurred while sending the promise3",err = result);
        }

        // Construct the requested resource.
        http:Response response = new;
        json msg = { "response": { "name": "main resource" } };
        response.setPayload(msg);

        // Send the requested resource.
        var result = caller->respond(response);

        if (result is error) {
            log:printError("Error occurred while sending the response", err = result);
        }

        // Construct promised resource1.
        http:Response push1 = new;
        msg = { "push": { "name": "resource1" } };
        push1.setPayload(msg);

        // Push promised resource1.
        var result = caller->pushPromisedResponse(promise1, push1);

        if (result is error) {
            log:printError("Error occurred while sending the promised response1", err = result);
        }

        // Construct promised resource2.
        http:Response push2 = new;
        msg = { "push": { "name": "resource2" } };
        push2.setPayload(msg);

        // Push promised resource2.
        var result = caller->pushPromisedResponse(promise2, push2);

        if (result is error) {
            log:printError("Error occurred while sending the promised response2", err = result);
        }

        // Construct promised resource3.
        http:Response push3 = new;
        msg = { "push": { "name": "resource3" } };
        push3.setPayload(msg);

        // Push promised resource3.
        var result = caller->pushPromisedResponse(promise3, push3);

        if (result is error){
           log:printError("Error occurred while sending the promised response3",err = result);
        }
    }
}
