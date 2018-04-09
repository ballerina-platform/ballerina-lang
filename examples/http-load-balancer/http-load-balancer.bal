import ballerina/http;

import ballerina/io;

endpoint http:Listener backendEP {
    port: 9090
};

endpoint http:Client lbEP {
    targets: [
             {url:"http://localhost:9090/mock1"},
             {url:"http://localhost:9090/mock2"},
             {url:"http://localhost:9090/mock3"}
             ]
};

@http:ServiceConfig {
    basePath: "/lb"
}
service<http:Service> loadBalancerService bind backendEP{

    @http:ResourceConfig {
        path:"/"
    }
    loadBalanceResource (endpoint conn, http:Request req) {
        http:Request outRequest = {};
        json requestPayload = {"name":"Ballerina"};
        outRequest.setJsonPayload(requestPayload);
        var response = lbEP -> post("/", outRequest);
        match  response {
                http:Response resp => _ = conn -> forward(resp);
                http:HttpConnectorError err => {
                http:Response outResponse = {};
                outResponse.statusCode = err.statusCode;
                outResponse.setStringPayload(err.message);
                _ = conn -> respond(outResponse);
            }
        }
    }
}

@http:ServiceConfig{basePath:"/mock1"}
service<http:Service> mock1 bind backendEP {
    @http:ResourceConfig {
        path:"/"
    }
    mock1Resource (endpoint conn, http:Request req) {
        http:Response outResponse = {};
        outResponse.setStringPayload("Mock1 Resource is invoked.");
        _ = conn -> respond(outResponse);
    }
}

@http:ServiceConfig{basePath:"/mock2"}
service<http:Service> mock2 bind backendEP {
    @http:ResourceConfig {
        path:"/"
    }
    mock2Resource (endpoint conn, http:Request req) {
        http:Response outResponse = {};
        outResponse.setStringPayload("Mock2 Resource is Invoked.");
        _ = conn -> respond(outResponse);
    }
}

@http:ServiceConfig{basePath:"/mock3"}
service<http:Service> mock3 bind backendEP {
    @http:ResourceConfig {
        path:"/"
    }
    mock3Resource (endpoint conn, http:Request req) {
        http:Response outResponse = {};
        outResponse.setStringPayload("Mock3 Resource is Invoked.");
        _ = conn -> respond(outResponse);
    }
}
