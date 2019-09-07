import ballerina/http;
import ballerina/log;
import ballerina/runtime;

listener http:Listener circuitBreakerEP07 = new(9315, { httpVersion: "2.0" });

http:ClientConfiguration conf07 = {
    circuitBreaker: {
        rollingWindow: {
            timeWindowInMillis: 60000,
            bucketSizeInMillis: 20000,
            requestVolumeThreshold: 0
        },
        failureThreshold: 0.3,
        resetTimeInMillis: 2000,
        statusCodes: [500, 501, 502, 503]
    },
    timeoutInMillis: 2000,
    httpVersion: "2.0"
};

http:Client backendClientEP07 = new("http://localhost:8095", conf07);

int cbTrialRequestCount = 0;

@http:ServiceConfig {
    basePath: "/cb"
}
service circuitbreaker07 on circuitBreakerEP07 {

    @http:ResourceConfig {
        path: "/trialrun"
    }
    resource function getState(http:Caller caller, http:Request request) {
        cbTrialRequestCount += 1;
        // To ensure the reset timeout period expires
        if (cbTrialRequestCount == 3) {
            runtime:sleep(3000);
        }
        var backendFuture = backendClientEP07->submit("GET", "/hello07", <@untainted> request);
        if (backendFuture is http:HttpFuture) {
            var backendRes = backendClientEP07->getResponse(backendFuture);
            if (backendRes is http:Response) {
                var responseToCaller = caller->respond(backendRes);
                if (responseToCaller is error) {
                    log:printError("Error sending response", responseToCaller);
                }
            } else {
                sendCBErrorResponse(caller, <error>backendRes);
            }
        } else {
            sendCBErrorResponse(caller, <error>backendFuture);
        }
    }
}

int cbTrialActualCount = 0;

@http:ServiceConfig { basePath: "/hello07" }
service helloService07 on new http:Listener(8095) {
    @http:ResourceConfig {
        methods: ["GET", "POST"],
        path: "/"
    }
    resource function sayHello(http:Caller caller, http:Request req) {
        cbTrialActualCount += 1;
        http:Response res = new;
        if (cbTrialActualCount == 1 || cbTrialActualCount == 2) {
            res.statusCode = http:STATUS_SERVICE_UNAVAILABLE;
            res.setPayload("Service unavailable.");
        } else {
            res.setPayload("Hello World!!!");
        }
        var responseToCaller = caller->respond(res);
        if (responseToCaller is error) {
            log:printError("Error sending response from mock service", responseToCaller);
        }
    }
}

function sendCBErrorResponse(http:Caller caller, error e) {
    http:Response response = new;
    response.statusCode = http:STATUS_INTERNAL_SERVER_ERROR;
    string errCause = <string> e.detail()?.message;
    response.setPayload(errCause);
    var responseToCaller = caller->respond(response);
    if (responseToCaller is error) {
        log:printError("Error sending response", responseToCaller);
    }
}
