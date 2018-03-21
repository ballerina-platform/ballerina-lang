import ballerina/net.http;

endpoint<http:Service> serviceEnpoint {
    port:9090
}

endpoint<http:Client> nasdaqEP {
    serviceUri: "http://localhost:9090/nasdaqStocks"
}

endpoint<http:Client> nyseEP {
    serviceUri: "http://localhost:9090/nyseStocks"
}

@http:serviceConfig {
    basePath:"/cbr",
    endpoints:[serviceEnpoint]
}
service<http:Service> contentBasedRouting {

    @http:resourceConfig {
        methods:["POST"],
        path:"/"
    }
    resource cbrResource (http:ServerConnector conn, http:Request req) {
        string nyseString = "nyse";
        var jsonMsg, _ = req.getJsonPayload();
        var nameString, _ = (string)jsonMsg.name;

        http:Request clientRequest = {};
        http:Response clientResponse = {};
        http:HttpConnectorError err;
        if (nameString == nyseString) {
            clientResponse, err = nyseEP -> post("/stocks", clientRequest);
        } else {
            clientResponse, err = nasdaqEP -> post("/stocks", clientRequest);
        }
        _ = conn -> forward(clientResponse);
    }
}

@http:serviceConfig {
    basePath:"/hbr",
    endpoints:[serviceEnpoint]
}
service<http:Service> headerBasedRouting {

    @http:resourceConfig {
        methods:["GET"],
        path:"/"
    }
    resource hbrResource (http:ServerConnector conn, http:Request req) {
        string nyseString = "nyse";
        var nameString = req.getHeader("name");

        http:Request clientRequest = {};
        http:Response clientResponse = {};
        http:HttpConnectorError err;
        if (nameString == nyseString) {
            clientResponse, err = nyseEP -> post("/stocks", clientRequest);
        } else {
            clientResponse, err = nasdaqEP -> post("/stocks", clientRequest);
        }
        _ = conn -> forward(clientResponse);
    }
}

@http:serviceConfig {
    basePath:"/nasdaqStocks",
    endpoints:[serviceEnpoint]
}
service<http:Service> nasdaqStocksQuote {

    @http:resourceConfig {
        methods:["POST"]
    }
    resource stocks (http:ServerConnector conn, http:Request req) {
        json payload = {"exchange":"nasdaq", "name":"IBM", "value":"127.50"};
        http:Response res = {};
        res.setJsonPayload(payload);
        _ = conn -> respond(res);
    }
}

@http:serviceConfig {
    basePath:"/nyseStocks",
    endpoints:[serviceEnpoint]
}
service<http:Service> nyseStockQuote {

    @http:resourceConfig {
        methods:["POST"]
    }
    resource stocks (http:ServerConnector conn, http:Request req) {
        json payload = {"exchange":"nyse", "name":"IBM", "value":"127.50"};
        http:Response res = {};
        res.setJsonPayload(payload);
        _ = conn -> respond(res);
    }
}