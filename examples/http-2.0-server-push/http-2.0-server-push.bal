import ballerina/http;
import ballerina/log;

endpoint http:Listener http2ServiceEP {
    port:7090,
    // HTTP version is set to 2.0.
    httpVersion:"2.0"
};

@http:ServiceConfig {
    basePath:"/http2Service"
}
service<http:Service> http2Service bind http2ServiceEP {

    @http:ResourceConfig {
        path:"/"
    }
    http2Resource(endpoint caller, http:Request req) {

        log:printInfo("Request received");

        // Send a Push Promise.
        http:PushPromise promise1 = new(path = "/resource1", method = "GET");
        caller->promise(promise1) but {
            error e => log:printError("Error occurred while sending the promise1", err = e) };

        // Send another Push Promise.
        http:PushPromise promise2 = new(path = "/resource2", method = "GET");
        caller->promise(promise2) but {
            error e => log:printError("Error occurred while sending the promise2", err = e) };

        // Send one more Push Promise.
        http:PushPromise promise3 = new(path = "/resource3", method = "GET");
        caller->promise(promise3) but {
            error e => log:printError("Error occurred while sending the promise3", err = e) };

        // Construct requested resource.
        http:Response response = new;
        json msg = {"response":{"name":"main resource"}};
        response.setJsonPayload(msg);

        // Send the requested resource.
        caller->respond(response) but {
            error e => log:printError("Error occurred while sending the response", err = e) };

        // Construct promised resource1.
        http:Response push1 = new;
        msg = {"push":{"name":"resource1"}};
        push1.setJsonPayload(msg);

        // Push promised resource1.
        caller->pushPromisedResponse(promise1, push1) but {
            error e => log:printError("Error occurred while sending the promised response1", err = e) };
        http:Response push2 = new;
        msg = {"push":{"name":"resource2"}};
        push2.setJsonPayload(msg);

        // Push promised resource2.
        caller->pushPromisedResponse(promise2, push2) but {
            error e => log:printError("Error occurred while sending the promised response2", err = e) };

        http:Response push3 = new;
        msg = {"push":{"name":"resource3"}};
        push3.setJsonPayload(msg);

        // Push promised resource3.
        caller->pushPromisedResponse(promise3, push3) but {
            error e => log:printError("Error occurred while sending the promised response3", err = e) };
    }
}

endpoint http:Client clientEP {
    url:"http://localhost:7090",
    // HTTP version is set to 2.0.
    httpVersion:"2.0"
};

function main(string... args) {

    http:Request serviceReq = new;
    http:HttpFuture httpFuture = new;
    // Submit a request.
    var submissionResult = clientEP->submit("GET", "/http2Service", serviceReq);
    match submissionResult {
        http:HttpFuture resultantFuture => {
            httpFuture = resultantFuture;
        }
        http:HttpConnectorError resultantErr => {
            log:printError("Error occurred while submitting a request", err = resultantErr);
            return;
        }
    }

    http:PushPromise[] promises = [];
    int promiseCount = 0;
    // Check whether promises exists.
    boolean hasPromise = clientEP->hasPromise(httpFuture);
    while (hasPromise) {
        http:PushPromise pushPromise = new;
        // Get the next promise.
        var nextPromiseResult = clientEP->getNextPromise(httpFuture);
        match nextPromiseResult {
            http:PushPromise resultantPushPromise => {
                pushPromise = resultantPushPromise;
            }
            http:HttpConnectorError resultantErr => {
                log:printError("Error occurred while fetching a push promise", err = resultantErr);
                return;
            }
        }
        log:printInfo("Received a promise for " + pushPromise.path);

        if (pushPromise.path == "/resource2") {
            // The client is not interested in receiving `/resource2` so, reject the promise.
            clientEP->rejectPromise(pushPromise);
            log:printInfo("Push promise for resource2 rejected");
        } else {
            // Store required promises.
            promises[promiseCount] = pushPromise;
            promiseCount = promiseCount + 1;
        }
        hasPromise = clientEP->hasPromise(httpFuture);
    }

    http:Response res = new;
    // Get the requested resource.
    var result = clientEP->getResponse(httpFuture);
    match result {
        http:Response resultantResponse => {
            res = resultantResponse;
        }
        http:HttpConnectorError resultantErr => {
            log:printError("Error occurred while fetching response", err = resultantErr);
            return;
        }
    }

    var responsePayload = res.getJsonPayload();
    match responsePayload {
        json resultantJsonPayload => {log:printInfo("Response : " + resultantJsonPayload.toString());}
        http:PayloadError e => {log:printError("Expected response payload not received", err = e);}
    }

    // Fetch required promised responses.
    foreach promise in promises {
        http:Response promisedResponse = new;
        var promisedResponseResult = clientEP->getPromisedResponse(promise);
        match promisedResponseResult {
            http:Response resultantPromisedResponse => {
                promisedResponse = resultantPromisedResponse;
            }
            http:HttpConnectorError resultantErr => {
                log:printError("Error occurred while fetching promised response", err = resultantErr);
                return;
            }
        }

        var promisedPayload = promisedResponse.getJsonPayload();
        match promisedPayload {
            json promisedJsonPayload => {log:printInfo("Promised resource : " + promisedJsonPayload.toString());}
            http:PayloadError e => {log:printError("Expected promised response payload not received", err = e);}
        }
    }
}
