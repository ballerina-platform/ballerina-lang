import ballerina/http;
import ballerina/runtime;
import ballerina/io;

endpoint http:Listener passthruEP {
    port:9090
};

endpoint http:Listener backendEP {
    port:8080
};

endpoint http:Client backendClientEP {
    url: "http://localhost:8080",
    circuitBreaker: {
        rollingWindow: {
            timeWindowMillis:10000,
            bucketSizeMillis:2000
        },
        failureThreshold:0.2,
        resetTimeMillis:10000,
        statusCodes:[400, 404, 500]
    },
    timeoutMillis:2000
};

@http:ServiceConfig {
    basePath:"/cb"
}
service<http:Service> circuitbreaker bind passthruEP {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/"
    }
    passthru (endpoint caller, http:Request request) {
        var backendRes = backendClientEP -> forward("/hello", request);
        match backendRes {
            http:Response res => {
            _ = caller -> respond(res);}
            http:HttpConnectorError httpConnectorError => {
            http:Response response = new;
            response.statusCode = 500;
            response.setStringPayload(httpConnectorError.message);
            _ = caller -> respond(response);}
        }
    }
}


public  int counter = 1;

// This sample service can be used to mock connection timeouts and service outages. 
// Service outage can be mocked by stopping/starting this service.
// This should be run separately from the `circuitBreakerDemo` service.
@http:ServiceConfig {basePath:"/hello"}
service<http:Service> helloWorld bind backendEP {
    @http:ResourceConfig {
        methods:["GET"],
        path:"/"
    }
    sayHello (endpoint caller, http:Request req) {
        if (counter % 5 == 0) {
            runtime:sleep(5000);
            counter = counter + 1;
            http:Response res = new;
            res.setStringPayload("Hello World!!!");
            _ = caller -> respond(res);
        } else if (counter % 5 == 3) {
            counter = counter + 1;
            http:Response res = new;
            res.statusCode = 500;
            res.setStringPayload("Internal error occurred while processing the request.");
            _ = caller -> respond(res);
        } else {
            counter = counter + 1;
            http:Response res = new;
            res.setStringPayload("Hello World!!!");
            _ = caller -> respond(res);
        }
    }
}
