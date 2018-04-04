import ballerina/http;
import ballerina/runtime;
import ballerina/io;

endpoint http:ServiceEndpoint passthruEP {
    port:9090
};

endpoint http:ServiceEndpoint backendEP {
    port:8080
};

endpoint http:ClientEndpoint backendClientEP {
    circuitBreaker: {
        rollingWindow: {
                            timeWindow:10000,
                            bucketSize:2000
                       },
        failureThreshold:0.2,
        resetTimeout:10000,
        statusCodes:[400, 404, 500]
    },
    targets: [
                 {
                     url: "http://localhost:8080"
                 }
             ],
    endpointTimeout:2000
};

@http:ServiceConfig {
    basePath:"/cb"
}
service<http:Service> circuitbreaker bind passthruEP {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/"
    }
    passthru (endpoint client, http:Request request) {
        http:Response response = {};
        http:HttpConnectorError err = {};
        var backendRes = backendClientEP -> forward("/hello", request);
        match backendRes {
            http:Response res => {
            _ = client -> forward(res);}
            http:HttpConnectorError err1 => {
             response = {};
            response.statusCode = 500;
            response.setStringPayload(err1.message);
            _ = client -> respond(response);}
        }
    }
}


public  int counter = 1;

// This sample service can be used to mock connection timeouts and service outages. Service outage can be mocked by stopping/starting this service.
// This should be run separately from the circuitBreakerDemo service.
@http:ServiceConfig {basePath:"/hello"}
service<http:Service> helloWorld bind backendEP {
    @http:ResourceConfig {
        methods:["GET"],
        path:"/"
    }
    sayHello (endpoint client, http:Request req) {
        if (counter % 5 == 0) {
            runtime:sleepCurrentWorker(5000);
            counter = counter + 1;
            http:Response res = {};
            res.setStringPayload("Hello World!!!");
            _ = client -> respond(res);
        } else if (counter % 5 == 3) {
            counter = counter + 1;
            http:Response res = {};
            res.statusCode = 500;
            res.setStringPayload("Internal erro r occurred while processing the request.");
            _ = client -> respond(res);
        } else {
            counter = counter + 1;
            http:Response res = {};
            res.setStringPayload("Hello World!!!");
            _ = client -> respond(res);
        }
    }
}
