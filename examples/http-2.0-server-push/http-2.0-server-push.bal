import ballerina/http;
import ballerina/io;

endpoint http:Listener ep {
   port:7090,
   // HTTP version is set to 2.0.
   httpVersion:"2.0"
};

@http:ServiceConfig {
    basePath:"/http2Service"
}
service<http:Service> http2Service bind ep {

 @http:ResourceConfig {
    path:"/main"
 }
 http2Resource (endpoint client, http:Request req) {

    io:println("Request received");

    // Send a Push Promise.
    http:PushPromise promise1 = new;
    promise1.path = "/resource1";
    promise1.method = "POST";
    _ = client -> promise(promise1);

    // Send another Push Promise.
    http:PushPromise promise2 = new;
    promise2.path = "/resource2";
    promise2.method = "POST";
    _ = client -> promise(promise2);

    // Send one more Push Promise.
    http:PushPromise promise3 = new;
    promise3.path = "/resource3";
    promise3.method = "POST";
    _ = client -> promise(promise3);

    // Construct requested resource.
    http:Response response = new;
    json msg = {"response":{"name":"main resource"}};
    response.setJsonPayload(msg);

    // Send the requested resource.
    _ = client -> respond(response);

    // Construct promised resource1.
    http:Response push1 = new;
    msg = {"push":{"name":"resource1"}};
    push1.setJsonPayload(msg);

    // Push promised resource1.
    _ = client -> pushPromisedResponse(promise1, push1);

    http:Response push2 = new;
    msg = {"push":{"name":"resource2"}};
    push2.setJsonPayload(msg);

    // Push promised resource2.
    _ = client -> pushPromisedResponse(promise2, push2);

    http:Response push3 = new;
    msg = {"push":{"name":"resource3"}};
    push3.setJsonPayload(msg);

    // Push promised resource3.
    _ = client -> pushPromisedResponse(promise3, push3);
  }
}

endpoint http:Client clientEP {
    targets: [
        {
            url: "http://localhost:7090"
        }
    ],
    // HTTP version is set to 2.0.
    httpVersion:"2.0"
};

function main (string... args) {

    http:Request serviceReq = new;
    http:HttpFuture httpFuture = new;
    // Submit a request.
    var submissionResult = clientEP -> submit("GET", "/http2Service/main", serviceReq);
    match submissionResult {
        http:HttpConnectorError err => {
            io:println("Error occurred while submitting a request");
            return;
        }
        http:HttpFuture resultantFuture => {
            httpFuture = resultantFuture;
        }
    }

    http:PushPromise[] promises = [];
    int promiseCount = 0;
    // Check whether promises exists.
    boolean hasPromise = clientEP -> hasPromise(httpFuture);
    while (hasPromise) {
        http:PushPromise pushPromise = new;
        // Get the next promise.
        var nextPromiseResult = clientEP -> getNextPromise(httpFuture);
        match nextPromiseResult {
            http:PushPromise resultantPushPromise => {
                pushPromise = resultantPushPromise;
            }
            http:HttpConnectorError err => {
                io:println("Error occurred while fetching push promise");
                return;
            }
        }
        io:println("Received a promise for " + pushPromise.path);

        if (pushPromise.path == "/resource2") {
            // Client is not interested of getting '/resource2', So reject the promise.
            clientEP -> rejectPromise(pushPromise);
            io:println("Push promise for resource2 rejected");
        } else {
            // Store required promises.
            promises[promiseCount] = pushPromise;
            promiseCount = promiseCount + 1;
        }
        hasPromise = clientEP -> hasPromise(httpFuture);
    }

    http:Response res = new;
    // Get the requested resource.
    var result = clientEP -> getResponse(httpFuture);
    match result {
        http:Response resultantResponse => {
            res = resultantResponse;
        }
        http:HttpConnectorError err => {
            io:println("Error occurred while fetching response");
            return;
        }
    }

    var responsePayload = res.getJsonPayload();
    match responsePayload {
        json resultantJsonPayload => {
            io:println("Response : " + resultantJsonPayload.toString());
        }
        http:PayloadError err => {
            io:println("Expected response not received");
        }
    }

    // Fetch required promised responses.
    foreach promise in promises {
        http:Response promisedResponse = new;
        var promisedResponseResult = clientEP -> getPromisedResponse(promise);
        match promisedResponseResult {
            http:Response resultantPromisedResponse => {
                promisedResponse = resultantPromisedResponse;
            }
            http:HttpConnectorError err => {
                io:println("Error occurred while fetching promised response");
                return;
            }
        }

        var promisedPayload = promisedResponse.getJsonPayload();
        match promisedPayload {
            json promisedJsonPayload => {
                io:println("Promised resource : " + promisedJsonPayload.toString());
            }
            http:PayloadError err => {
                io:println("Promised response not received");
            }
        }
    }
}
