import ballerina/http;

endpoint http:Listener passthroughEP1 {
    port:9113
};

endpoint http:Client nyseEP1 {
    url:"http://localhost:9113"
};

@http:ServiceConfig {basePath:"/passthrough"}
service<http:Service> passthroughService bind passthroughEP1 {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/"
    }
    passthrough (endpoint caller, http:Request clientRequest) {
        var response = nyseEP1 -> get("/nyseStock/stocks", message = untaint clientRequest);
        match response {
            http:Response httpResponse => {
                _ = caller -> respond(httpResponse);
            }
            error err => {
                http:Response errorResponse = new;
                json errMsg = {"error":"error occurred while invoking the service"};
                errorResponse.setJsonPayload(errMsg);
                _ = caller -> respond(errorResponse);
            }
        }
    }
}

@http:ServiceConfig {basePath:"/nyseStock"}
service<http:Service> nyseStockQuote1 bind passthroughEP1 {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/stocks"
    }
    stocks (endpoint caller, http:Request clientRequest) {
        http:Response res = new;
        json payload = {"exchange":"nyse", "name":"IBM", "value":"127.50"};
        res.setJsonPayload(payload);
        _ = caller -> respond(res);
    }
}
