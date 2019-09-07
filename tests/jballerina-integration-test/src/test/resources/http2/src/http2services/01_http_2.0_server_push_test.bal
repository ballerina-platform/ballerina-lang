import ballerina/io;
import ballerina/http;
import ballerina/stringutils;

listener http:Listener frontendEP = new(9090);

http:Client backendClientEP = new("http://localhost:7090", { httpVersion: "2.0" });

@http:ServiceConfig {
    basePath: "/frontend"
}
service frontendHttpService on frontendEP {

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/"
    }
    resource function frontendHttpResource(http:Caller caller, http:Request clientRequest) {

        http:Request serviceReq = new;
        http:HttpFuture httpFuture = new;
        // Submit a request
        var submissionResult = backendClientEP->submit("GET", "/backend/main", serviceReq);
        if (submissionResult is http:HttpFuture) {
            httpFuture = submissionResult;
        } else {
            io:println("Error occurred while submitting a request");
            json errMsg = { "error": "error occurred while submitting a request" };
            checkpanic caller->respond(errMsg);
            return;
        }

        // Check whether promises exists
        http:PushPromise?[] promises = [];
        int promiseCount = 0;
        boolean hasPromise = backendClientEP->hasPromise(httpFuture);
        while (hasPromise) {
            http:PushPromise pushPromise = new;
            // Get the next promise
            var nextPromiseResult = backendClientEP->getNextPromise(httpFuture);
            if (nextPromiseResult is http:PushPromise) {
                pushPromise = nextPromiseResult;
            } else {
                io:println("Error occurred while fetching a push promise");
                json errMsg = { "error": "error occurred while fetching a push promise" };
                checkpanic caller->respond(errMsg);
                return;
            }

            io:println("Received a promise for " + pushPromise.path);
            // Store required promises
            promises[promiseCount] = pushPromise;
            promiseCount = promiseCount + 1;
            hasPromise = backendClientEP->hasPromise(httpFuture);
        }
        // By this time 3 promises should be received, if not send an error response
        if (promiseCount != 3) {
            json errMsg = { "error": "expected number of promises not received" };
            checkpanic caller->respond(errMsg);
            return;
        }
        io:println("Number of promises received : " + promiseCount.toString());

        // Get the requested resource
        http:Response response = new;
        var result = backendClientEP->getResponse(httpFuture);
        if (result is http:Response) {
            response = result;
        } else {
            io:println("Error occurred while fetching response");
            json errMsg = { "error": "error occurred while fetching response" };
            checkpanic caller->respond(errMsg);
            return;
        }

        var responsePayload = response.getJsonPayload();
        json responseJsonPayload = {};
        if (responsePayload is json) {
            responseJsonPayload = responsePayload;
        } else {
            json errMsg = { "error": "expected response message not received" };
            checkpanic caller->respond(errMsg);
            return;
        }
        // Check whether correct response received
        string responseStringPayload = responseJsonPayload.toString();
        if (!(stringutils:contains(responseStringPayload, "main"))) {
            json errMsg = { "error": "expected response message not received" };
            checkpanic caller->respond(errMsg);
            return;
        }
        io:println("Response : " + responseStringPayload);

        // Fetch required promised responses
        foreach var p in promises {
            http:PushPromise promise = <http:PushPromise>p;
            http:Response promisedResponse = new;
            var promisedResponseResult = backendClientEP->getPromisedResponse(promise);
            if (promisedResponseResult is http:Response) {
                promisedResponse = promisedResponseResult;
            } else {
                io:println("Error occurred while fetching promised response");
                json errMsg = { "error": "error occurred while fetching promised response" };
                checkpanic caller->respond(errMsg);
                return;
            }

            json promisedJsonPayload = {};
            var promisedPayload = promisedResponse.getJsonPayload();
            if (promisedPayload is json) {
                promisedJsonPayload = promisedPayload;
            } else {
                json errMsg = { "error": "expected promised response not received" };
                checkpanic caller->respond(errMsg);
                return;
            }

            // check whether expected
            string expectedVal = promise.path.substring(1, 10);
            string promisedStringPayload = promisedJsonPayload.toString();
            if (!(stringutils:contains(promisedStringPayload, expectedVal))) {
                json errMsg = { "error": "expected promised response not received" };
                checkpanic caller->respond(errMsg);
                return;
            }
            io:println("Promised resource : " + promisedStringPayload);
        }

        // By this time everything has went well, hence send a success response
        json successMsg = { "status": "successful" };
        checkpanic caller->respond(successMsg);
    }
}

listener http:Listener backendEP = new(7090, { httpVersion: "2.0" });

@http:ServiceConfig {
    basePath: "/backend"
}
service backendHttp2Service on backendEP {

    @http:ResourceConfig {
        path: "/main"
    }
    resource function backendHttp2Resource(http:Caller caller, http:Request req) {

        io:println("Request received");

        // Send a Push Promise
        http:PushPromise promise1 = new("/resource1", "POST");
        checkpanic caller->promise(promise1);

        // Send another Push Promise
        http:PushPromise promise2 = new("/resource2", "POST");
        checkpanic caller->promise(promise2);

        // Send one more Push Promise
        http:PushPromise promise3 = new;
        // create with default params
        promise3.path = "/resource3";
        // set parameters
        promise3.method = "POST";
        checkpanic caller->promise(promise3);

        // Construct requested resource
        json msg = { "response": { "name": "main resource" } };

        // Send the requested resource
        checkpanic caller->respond(msg);

        // Construct promised resource1
        http:Response push1 = new;
        msg = { "push": { "name": "resource1" } };
        push1.setJsonPayload(msg);

        // Push promised resource1
        checkpanic caller->pushPromisedResponse(promise1, push1);

        http:Response push2 = new;
        msg = { "push": { "name": "resource2" } };
        push2.setJsonPayload(msg);

        // Push promised resource2
        checkpanic caller->pushPromisedResponse(promise2, push2);

        http:Response push3 = new;
        msg = { "push": { "name": "resource3" } };
        push3.setJsonPayload(msg);

        // Push promised resource3
        checkpanic caller->pushPromisedResponse(promise3, push3);
    }
}
