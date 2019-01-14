import ballerina/http;
import ballerina/log;
import ballerina/runtime;
import ballerinax/kubernetes;

@kubernetes:Ingress {
    hostname: "scenariotests.ballerina.io",
    name: "circuit_breaker_frontend_service",
    path: "/"
}

@kubernetes:Service {
    serviceType:"LoadBalancer",
    port:80,
    name: "circuit_breaker_frontend_service"
}

@kubernetes:Deployment {
    image: "scenariotests.ballerina.io/circuit_breaker_frontend_service:v1.0",
    baseImage: "ballerina/ballerina:0.990.2",
    name: "circuit_breaker_frontend_service"
}
listener http:Listener httpListener = new(9090);

http:Client backendClientEP = new("http://ballerina-circuit-breaker-backend-service:8080", config = {
        circuitBreaker: {
            rollingWindow: {
                timeWindowMillis: 100000,
                bucketSizeMillis: 20000,
                requestVolumeThreshold: 0

            },
            failureThreshold: 0.2,
            resetTimeMillis: 10000,
            statusCodes: [500, 501, 502, 503]
        },
        timeoutMillis: 8000
    });

// Create an HTTP service bound to the endpoint (circuitBreakerEP).
@http:ServiceConfig {
    basePath: "/cb"
}
service circuitbreaker on httpListener {
    // Create a REST resource within the API.
    @http:ResourceConfig {
        methods: ["GET"],
        path: "/"
    }
    // The parameters include a reference to the caller
    // and an object of the request data.
    resource function invokeEndpoint(http:Caller caller, http:Request request) {

        var backendResponse = backendClientEP->forward("/hello", request);

        if (backendResponse is http:Response) {
            var responseToCaller = caller->respond(backendResponse);
            if (responseToCaller is error) {
                log:printError("Error sending response", err = responseToCaller);
            }
        } else {
            http:Response response = new;
            response.statusCode = http:INTERNAL_SERVER_ERROR_500;
            response.setPayload(<string> backendResponse.detail().message);
            var responseToCaller = caller->respond(response);
            if (responseToCaller is error) {
                log:printError("Error sending response", err = responseToCaller);
            }
        }
    }
}
