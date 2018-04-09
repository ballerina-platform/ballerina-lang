import ballerina/http;

endpoint http:ServiceEndpoint passthroughEP {
    port:9090
};

endpoint http:Client nyseEP {
    targets:[{url:"http://localhost:9090"}]
};

@http:ServiceConfig {basePath:"/passthrough"}
service<http:Service> passthroughService bind passthroughEP {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/"
    }
    passthrough (endpoint outboundEP, http:Request clientRequest) {
        var response = nyseEP -> get("/nyseStock/stocks", clientRequest);
        match response {
            http:Response httpResponse => {
                _ = outboundEP -> forward(httpResponse);
            }
            http:HttpConnectorError err => {
                http:Response errorResponse = new;
                json errMsg = {"error":"error occurred while invoking the service"};
                errorResponse.setJsonPayload(errMsg);
                _ = outboundEP -> respond(errorResponse);
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
    stocks (endpoint outboundEP, http:Request clientRequest) {
        http:Response res = new;
        json payload = {"exchange":"nyse", "name":"IBM", "value":"127.50"};
        res.setJsonPayload(payload);
        _ = outboundEP -> respond(res);
    }
}
