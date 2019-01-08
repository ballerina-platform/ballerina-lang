import ballerina/http;
import ballerina/log;
import ballerina/runtime;
import ballerinax/kubernetes;

@kubernetes:Ingress {
    hostname: "scenariotests.ballerina.io",
    name: "ballerina-circuit-breaker-backend-service",
    path: "/"
}

@kubernetes:Service {
    serviceType:"NodePort",
    name: "ballerina-circuit-breaker-backend-service"
}

@kubernetes:Deployment {
    image: "scenariotests.ballerina.io/circuit_breaker_backend_service:v1.0",
    baseImage: "ballerina/ballerina:0.990.2",
    name: "ballerina-circuit-breaker-backend-service"
}
listener http:Listener mockServiceListener = new(8080);

public int counter = 1;

// This sample service is used to mock connection timeouts and service outages.
// Mock a service outage by stopping/starting this service.
// This should run separately from the `circuitBreakerDemo` service.
@http:ServiceConfig { basePath: "/hello" }
service helloWorld on mockServiceListener {
    @http:ResourceConfig {
        methods: ["GET"],
        path: "/"
    }
    resource function sayHello(http:Caller caller, http:Request req) {
        if (counter % 5 == 0) {
            counter = counter + 1;
            // Delay the response by 5000 milliseconds to
            // mimic the network level delays.
            runtime:sleep(5000);

            var result = caller->respond("Hello World!!!");
            if (result is error) {
               log:printError("Error sending response from mock service", err = result);
            }
        } else if (counter % 5 == 3) {
            counter = counter + 1;
            http:Response res = new;
            res.statusCode = 500;
            res.setPayload(
                    "Internal error occurred while processing the request.");
            var result = caller->respond(res);
            if (result is error) {
               log:printError("Error sending response from mock service", err = result);
            }
        } else {
            counter = counter + 1;
            var result = caller->respond("Hello World!!!");
            if (result is error) {
               log:printError("Error sending response from mock service", err = result);
            }
        }
    }
}
