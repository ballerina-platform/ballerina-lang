import ballerina/http;

endpoint http:Listener serviceEP {
    port:9090
};

endpoint http:LoadBalanceClient lbEP {
    targets:[
        {url:"http://localhost:9090/mock1"},
        {url:"http://localhost:9090/mock2"},
        {url:"http://localhost:9090/mock3"}
    ],
    algorithm:http:ROUND_ROBIN,
    timeoutMillis:5000
};

@http:ServiceConfig {
    basePath:"/lb"
}
service<http:Service> loadBalancerDemoService bind serviceEP {

    @http:ResourceConfig {
        path:"/"
    }
    loadBalanceResource(endpoint caller, http:Request req) {
        http:Request outRequest = new;
        json requestPayload = {"name":"Ballerina"};
        outRequest.setJsonPayload(requestPayload);
        var response = lbEP -> post("/", outRequest);
        match response {
            http:Response resp => _ = caller -> respond(resp);
            http:HttpConnectorError httpConnectorError => {
                http:Response outResponse = new;
                outResponse.statusCode = httpConnectorError.statusCode;
                outResponse.setStringPayload(httpConnectorError.message);
                _ = caller -> respond(outResponse);
            }
        }
    }
}

@http:ServiceConfig {basePath:"/mock1"}
service<http:Service> mock1 bind serviceEP {
    @http:ResourceConfig {
        path:"/"
    }
    mock1Resource(endpoint caller, http:Request req) {
        http:Response outResponse = new;
        outResponse.setStringPayload("Mock1 Resource is invoked.");
        _ = caller -> respond(outResponse);
    }
}

@http:ServiceConfig {basePath:"/mock2"}
service<http:Service> mock2 bind serviceEP {
    @http:ResourceConfig {
        path:"/"
    }
    mock2Resource(endpoint caller, http:Request req) {
        http:Response outResponse = new;
        outResponse.setStringPayload("Mock2 Resource is Invoked.");
        _ = caller -> respond(outResponse);
    }
}

@http:ServiceConfig {basePath:"/mock3"}
service<http:Service> mock3 bind serviceEP {
    @http:ResourceConfig {
        path:"/"
    }
    mock3Resource(endpoint caller, http:Request req) {
        http:Response outResponse = new;
        outResponse.setStringPayload("Mock3 Resource is Invoked.");
        _ = caller -> respond(outResponse);
    }
}
