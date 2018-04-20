import ballerina/http;
import ballerina/runtime;
import ballerina/io;

endpoint http:Listener serviceEP {
    port:9090
};

endpoint http:Listener backendEP {
    port:8080
};

endpoint http:FailoverClient ClientEP {
    timeoutMillis:5000,
    failoverCodes:[501, 502, 503],
    intervalMillis:5000,
    targets:[
        {
            url:"http://localhost:3000/mock1"
        },
        {
            url:"http://localhost:8080/echo"
        },
        {
            url:"http://localhost:8080/mock"
        }
    ]
};

@http:ServiceConfig {
    basePath:"/fo"
}
service<http:Service> failoverDemoService bind serviceEP {

    @http:ResourceConfig {
        methods:["GET", "POST"],
        path:"/"
    }
    doFailover(endpoint caller, http:Request request) {
        http:Response response = new;
        http:HttpConnectorError err = {};
        var backendRes = ClientEP -> get("/", request);
        match backendRes {
            http:Response res => {
                _ = caller -> respond(res);}
            http:HttpConnectorError httpConnectorError => {
                response = new;
                response.statusCode = 500;
                response.setStringPayload(httpConnectorError.message);
                _ = caller -> respond(response);}
        }
    }
}

@http:ServiceConfig {
    basePath:"/echo"
}
service<http:Service> echo bind backendEP {
    @http:ResourceConfig {
        methods:["POST", "PUT", "GET"],
        path:"/"
    }
    echoResource(endpoint caller, http:Request req) {
        http:Response outResponse = new;
        runtime:sleepCurrentWorker(30000);
        outResponse.setStringPayload("echo Resource is invoked");
        _ = caller -> respond(outResponse);
    }
}

@http:ServiceConfig {
    basePath:"/mock"
}
service<http:Service> mock bind backendEP {
    @http:ResourceConfig {
        methods:["POST", "PUT", "GET"],
        path:"/"
    }
    mockResource(endpoint caller, http:Request req) {
        http:Response outResponse = new;
        outResponse.setStringPayload("Mock Resource is Invoked.");
        _ = caller -> respond(outResponse);
    }
}
