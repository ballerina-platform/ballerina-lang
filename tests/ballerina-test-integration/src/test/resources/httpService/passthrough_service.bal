import ballerina/http;

endpoint http:Listener passthroughEP {
    port:9090
};

endpoint http:Client nyseEP {
    url:"http://localhost:9090"
};

@http:ServiceConfig {basePath:"/passthrough"}
service<http:Service> passthroughService bind passthroughEP {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/"
    }
    passthrough (endpoint caller, http:Request clientRequest) {
        var response = nyseEP -> get("/nyseStock/stocks", request = clientRequest);
        match response {
            http:Response httpResponse => {
                _ = caller -> respond(httpResponse);
            }
            http:HttpConnectorError err => {
                http:Response errorResponse = new;
                json errMsg = {"error":"error occurred while invoking the service"};
                errorResponse.setJsonPayload(errMsg);
                _ = caller -> respond(errorResponse);
            }
        }
    }
}

@http:ServiceConfig {basePath:"/nyseStock"}
service<http:Service> nyseStockQuote bind passthroughEP {

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
