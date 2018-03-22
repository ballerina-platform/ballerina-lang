import ballerina/net.http;

endpoint http:ServiceEndpoint passthroughEP {
    port:9090
};

endpoint http:ClientEndpoint nyseEP {
    targets: [{uri:"http://localhost:9090"}]
};

@http:ServiceConfig {basePath:"/passthrough"}
service<http:Service> passthroughService bind passthroughEP {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/"
    }
    passthrough (endpoint outboundEP, http:Request clientRequest) {
        http:Response response =? nyseEP -> get("/nyseStock/stocks", clientRequest);
        _ = outboundEP -> forward(response);
    }
}

@http:ServiceConfig {basePath:"/nyseStock"}
service<http:Service> nyseStockQuote bind passthroughEP {

    @http:ResourceConfig {
        methods:["GET"]
    }
    stocks (endpoint outboundEP, http:Request clientRequest) {
        http:Response res = {};
        json payload = {"exchange":"nyse", "name":"IBM", "value":"127.50"};
        res.setJsonPayload(payload);
        _ = outboundEP -> respond(res);
    }
}