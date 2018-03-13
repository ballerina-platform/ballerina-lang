import ballerina.net.http;
import ballerina.io;

endpoint<http:Service> ep {
    port:7090,
    // HTTP version is set to 2.0
    httpVersion:"2.0"
}

@http:serviceConfig {
    basePath:"/http2Service",
    endpoints:[ep]
}
service<http:Service> http2Service {

  @http:resourceConfig {
     path:"/main"
  }
  resource http2Resource (http:ServerConnector conn, http:Request req) {

    io:println("Request received");

    // Send a Push Promise
    http:PushPromise promise1 = {path:"/resource1", method:"POST"};
    _ = conn -> promise(promise1);

    // Send another Push Promise
    http:PushPromise promise2 = {path:"/resource2", method:"POST"};
    _ = conn -> promise(promise2);

    // Send one more Push Promise
    http:PushPromise promise3 = {path:"/resource3", method:"POST"};
    _ = conn -> promise(promise3);

    // Construct requested resource
    http:Response response = {};
    json msg = {"response":{"name":"main resource"}};
    response.setJsonPayload(msg);

    // Send the requested resource
    _ = conn -> respond(response);

    // Construct promised resource1
    http:Response push1 = {};
    msg = {"push":{"name":"resource1"}};
    push1.setJsonPayload(msg);

    // Push promised resource1
    _ = conn -> pushPromisedResponse(promise1, push1);

    http:Response push2 = {};
    msg = {"push":{"name":"resource2"}};
    push2.setJsonPayload(msg);

    // Push promised resource2
    _ = conn -> pushPromisedResponse(promise2, push2);

    http:Response push3 = {};
    msg = {"push":{"name":"resource3"}};
    push3.setJsonPayload(msg);

    // Push promised resource3
    _ = conn -> pushPromisedResponse(promise3, push3);
  }
}

endpoint<http:Client> clientEP {
    serviceUri: "http://localhost:7090",
    // HTTP version is set to 2.0
    httpVersion:"2.0"
}

function main (string[] args) {

    http:Request serviceReq = {};
    http:HttpHandle h = {};
    // Submit a request
    h, _ = clientEP -> submit("GET","/http2Service/main", serviceReq);

    http:PushPromise[] promises = [];
    int i = 0;
    // Check whether promises exists
    boolean hasPromise = clientEP -> hasPromise(h);
    while (hasPromise) {
        http:PushPromise promise = {};
        // Get the next promise
        promise, _ = clientEP -> getNextPromise(h);
        io:println("Received a promise for " + promise.path);

        if (promise.path == "/resource2") {
            // Client is not interested of getting '/resource2'. So reject the promise
            _ = clientEP -> rejectPromise(promise);
            io:println("Push promise for resource2 rejected");
        } else {
            // Store required promises
            promises[i] = promise;
            i = i + 1;
        }
        hasPromise = clientEP -> hasPromise(h);
    }

    http:Response res = {};
    // Get the requested resource
    res, _ = clientEP -> getResponse(h);
    json responsePayload;
    responsePayload, _ = res.getJsonPayload();
    io:println("Response : " + responsePayload.toString());

    // Fetch required promised responses
    foreach p in promises {
        http:Response promisedResponse = {};
        promisedResponse, _ = clientEP -> getPromisedResponse(p);
        json payload;
        payload, _ = promisedResponse.getJsonPayload();
        io:println("Promised resource : " + payload.toString());
    }
}