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
    serviceType:"LoadBalancer",
    port:80,
    name: "ballerina-circuit-breaker-backend-service"
}

@kubernetes:Deployment {
    image: "ballerinascenarios/circuit_breaker_backend_service:v1.0",
    baseImage: "ballerina/ballerina:0.990.2",
    name: "ballerina-circuit-breaker-backend-service",
    username:"ballerinascenarios",
    password:"ballerina75389",
    push:true
}
listener http:Listener mockServiceListener = new(8080);

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
        var result = caller->respond("Hello World!!!");
        if (result is error) {
           log:printError("Error sending response from mock service", err = result);
        }
    }
}