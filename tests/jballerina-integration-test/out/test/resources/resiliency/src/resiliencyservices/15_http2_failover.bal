import ballerina/http;
import ballerina/log;
import ballerina/runtime;

listener http:Listener failoverEP06 = new(9314, { httpVersion: "2.0" });

listener http:Listener backendEP06 = new(8094, { httpVersion: "2.0" });

http:FailoverClient foBackendEP06 = new({
    timeoutInMillis: 5000,
    failoverCodes: [500, 501, 502, 503],
    intervalInMillis: 5000,
    httpVersion: "2.0",
    // Define set of HTTP Clients that needs to be Failover.
    targets: [
        { url: "http://localhost:8094/delay" },
        { url: "http://localhost:8094/error" },
        { url: "http://localhost:8094/mock" }
    ]
});

@http:ServiceConfig {
    basePath: "/fo"
}
service failoverDemoService06 on failoverEP06 {
    @http:ResourceConfig {
        methods: ["GET", "POST"],
        path: "/index"
    }
    resource function failoverStartIndex(http:Caller caller, http:Request request) {
        string startIndex = foBackendEP06.succeededEndpointIndex.toString();
        var backendRes = foBackendEP06->submit("GET", "/", <@untainted> request);
        if (backendRes is http:HttpFuture) {
            var response = foBackendEP06->getResponse(backendRes);
            if (response is http:Response) {
                string responseMessage = "Failover start index is : " + startIndex;
                var responseToCaller = caller->respond(responseMessage);
                handleResponseToCaller(responseToCaller);
            } else {
                sendErrorResponse(caller, <error>response);
            }
        } else {
            sendErrorResponse(caller, <error>backendRes);
        }
    }
}

// Delayed service to mimic failing service due to network delay.
@http:ServiceConfig {
    basePath: "/delay"
}
service delay06 on backendEP06 {
    @http:ResourceConfig {
        methods: ["POST", "PUT", "GET"],
        path: "/"
    }
    resource function delayResource(http:Caller caller, http:Request req) {
        // Delay the response for 5000 milliseconds to mimic network level delays.
        runtime:sleep(10000);
        var responseToCaller = caller->respond("Delayed resource is invoked");
        if (responseToCaller is error) {
            log:printError("Error sending response from delay service", responseToCaller);
        }
    }
}

// Error service to mimic internal server error.
@http:ServiceConfig {
    basePath: "/error"
}
service error06 on backendEP06 {
    @http:ResourceConfig {
        methods: ["POST", "PUT", "GET"],
        path: "/"
    }
    resource function mockResource(http:Caller caller, http:Request req) {
        http:Response response = new;
        response.statusCode = 500;
        response.setPayload("Response from error Service with error status code.");
        var responseToCaller = caller->respond(response);
        if (responseToCaller is error) {
            log:printError("Error sending response from error service", responseToCaller);
        }
    }
}

// Mock service to mimic healthy service.
@http:ServiceConfig {
    basePath: "/mock"
}
service mock06 on backendEP06 {
    @http:ResourceConfig {
        methods: ["POST", "PUT", "GET"],
        path: "/"
    }
    resource function mockResource(http:Caller caller, http:Request req) {
        var responseToCaller = caller->respond("Mock Resource is Invoked.");
        if (responseToCaller is error) {
            log:printError("Error sending response from mock service", responseToCaller);
        }
    }
}

function handleResponseToCaller(error? responseToCaller) {
    if (responseToCaller is error) {
        log:printError("Error sending response from failover service.", responseToCaller);
    }
}

function sendErrorResponse(http:Caller caller, error e) {
    http:Response response = new;
    response.statusCode = 500;
    response.setPayload(<string>e.detail()?.message);
    var respondToCaller = caller->respond(response);
    handleResponseToCaller(respondToCaller);
}
