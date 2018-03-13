import ballerina.net.http;
import ballerina.io;

endpoint<http:Service> frontendEP {
    port:9090
}

endpoint<http:Client> backendClientEP {
    serviceUri: "http://localhost:7090",
    // HTTP version is set to 2.0
    httpVersion:"2.0"
}

@http:serviceConfig {
    basePath:"/frontend",
    endpoints:[frontendEP]
}
service<http:Service> frontendHttpService {

    @http:resourceConfig {
        methods:["GET"],
        path:"/"
    }
    resource frontendHttpResource (http:ServerConnector conn, http:Request clientRequest) {

        http:Request serviceReq = {};
        http:HttpHandle handle = {};
        // Submit a request
        handle, _ = backendClientEP -> submit("GET","/backend/main", serviceReq);

        // Check whether promises exists
        http:PushPromise[] promises = [];
        int promiseCount = 0;
        boolean hasPromise = backendClientEP -> hasPromise(handle);
        while (hasPromise) {
            http:PushPromise pushPromise = {};
            // Get the next promise
            pushPromise, _ = backendClientEP -> getNextPromise(handle);
            io:println("Received a promise for " + pushPromise.path);
            // Store required promises
            promises[promiseCount] = pushPromise;
            promiseCount = promiseCount + 1;
            hasPromise = backendClientEP -> hasPromise(handle);
        }
        // By this time 3 promises should be received, if not send an error response
        if (promiseCount != 3) {
            http:Response errorResponse = {};
            json errMsg = {"error":"expected number of promises not received"};
            errorResponse.setJsonPayload(errMsg);
            _ = conn -> respond(errorResponse);
            return;
        }
        io:println("Number of promises received : " + promiseCount);

        // Get the requested resource
        http:Response res = {};
        res, _ = backendClientEP -> getResponse(handle);
        json responsePayload;
        responsePayload, _ = res.getJsonPayload();
        // Check whether correct response received
        if (!(responsePayload.toString().contains("main"))) {
            http:Response errorResponse = {};
            json errMsg = {"error":"expected response message not received"};
            errorResponse.setJsonPayload(errMsg);
            _ = conn -> respond(errorResponse);
            return;
        }
        io:println("Response : " + responsePayload.toString());

        // Fetch required promised responses
        foreach promise in promises {
            http:Response promisedResponse = {};
            promisedResponse, _ = backendClientEP -> getPromisedResponse(promise);
            json payload;
            payload, _ = promisedResponse.getJsonPayload();
            // check whether expected
            string expectedVal = promise.path.subString(1, 10);
            if (!(payload.toString().contains(expectedVal))) {
                http:Response errorResponse = {};
                json errMsg = {"error":"expected promised response not received"};
                errorResponse.setJsonPayload(errMsg);
                _ = conn -> respond(errorResponse);
                return;
            }
            io:println("Promised resource : " + payload.toString());
        }

        // By this time everything has went well, hence send a success response
        http:Response successResponse = {};
        json successMsg = {"status":"successful"};
        successResponse.setJsonPayload(successMsg);
        _ = conn -> respond(successResponse);
    }
}

endpoint<http:Service> backendEP {
    port:7090,
    // HTTP version is set to 2.0
    httpVersion:"2.0"
}

@http:serviceConfig {
    basePath:"/backend",
    endpoints:[backendEP]
}
service<http:Service> backendHttp2Service {

  @http:resourceConfig {
     path:"/main"
  }
  resource backendHttp2Resource (http:ServerConnector conn, http:Request req) {

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
