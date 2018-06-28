import ballerina/http;
import ballerina/log;

// Create an HTTP client endpoint that can send HTTP/2 messages.
endpoint http:Client clientEP {
    url: "http://localhost:7090",
    // HTTP version is set to 2.0.
    httpVersion: "2.0"

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
        error resultantErr => {
            log:printError("Error occurred while submitting a request",
                            err = resultantErr);
            return;
        }
    }

    http:PushPromise[] promises = [];
    int promiseCount = 0;
    // Check if promises exists.
    boolean hasPromise = clientEP->hasPromise(httpFuture);

    while (hasPromise) {
        http:PushPromise pushPromise = new;
        // Get the next promise.
        var nextPromiseResult = clientEP->getNextPromise(httpFuture);

        match nextPromiseResult {
            http:PushPromise resultantPushPromise => {
                pushPromise = resultantPushPromise;
            }
            error resultantErr => {
                log:printError("Error occurred while fetching a push promise",
                                err = resultantErr);
                return;
            }
        }
        log:printInfo("Received a promise for " + pushPromise.path);

        if (pushPromise.path == "/resource2") {
            // The client is not interested in receiving `/resource2`.
            // Therefore, reject the promise.
            clientEP->rejectPromise(pushPromise);

            log:printInfo("Push promise for resource2 rejected");
        } else {
            // Store the required promises.
            promises[promiseCount] = pushPromise;

            promiseCount = promiseCount + 1;
        }
        hasPromise = clientEP->hasPromise(httpFuture);
    }

    http:Response response = new;
    // Get the requested resource.
    var result = clientEP->getResponse(httpFuture);

    match result {
        http:Response resultantResponse => {
            response = resultantResponse;
        }
        error resultantErr => {
            log:printError("Error occurred while fetching response",
                            err = resultantErr);
            return;
        }
    }

    var responsePayload = response.getJsonPayload();
    match responsePayload {
        json resultantJsonPayload =>
              log:printInfo("Response : " + resultantJsonPayload.toString());
        error e =>
              log:printError("Expected response payload not received", err = e);
    }

    // Fetch required promise responses.
    foreach promise in promises {
        http:Response promisedResponse = new;
        var promisedResponseResult = clientEP->getPromisedResponse(promise);
        match promisedResponseResult {
            http:Response resultantPromisedResponse => {
                promisedResponse = resultantPromisedResponse;
            }
            error resultantErr => {
                log:printError("Error occurred while fetching promised response",
                                err = resultantErr);
                return;
            }
        }
        var promisedPayload = promisedResponse.getJsonPayload();
        match promisedPayload {
            json promisedJsonPayload =>
                       log:printInfo("Promised resource : " +
                                      promisedJsonPayload.toString());
            error e =>
                  log:printError("Expected promised response payload not received",
                                  err = e);
        }
    }

}
