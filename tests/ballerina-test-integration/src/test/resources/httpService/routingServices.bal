import ballerina.net.http;

@http:configuration {basePath:"/cbr"}
service<http> contentBasedRouting {

    @http:resourceConfig {
        methods:["POST"],
        path:"/"
    }
    resource cbrResource (http:Connection conn, http:InRequest req) {
        endpoint<http:HttpClient> nasdaqEP {
            create http:HttpClient("http://localhost:9090/nasdaqStocks", {});
        }
        endpoint<http:HttpClient> nyseEP {
            create http:HttpClient("http://localhost:9090/nyseStocks", {});
        }
        string nyseString = "nyse";
        json jsonMsg = req.getJsonPayload();
        var nameString, _ = (string)jsonMsg.name;

        http:OutRequest clientRequest = {};
        http:InResponse clientResponse = {};
        http:HttpConnectorError err;
        if (nameString == nyseString) {
            clientResponse, err = nyseEP.post("/stocks", clientRequest);
        } else {
            clientResponse, err = nasdaqEP.post("/stocks", clientRequest);
        }
        _ = conn.forward(clientResponse);
    }
}

@http:configuration {basePath:"/hbr"}
service<http> headerBasedRouting {

    @http:resourceConfig {
        methods:["GET"],
        path:"/"
    }
    resource hbrResource (http:Connection conn, http:InRequest req) {
        endpoint<http:HttpClient> nasdaqEP {
            create http:HttpClient("http://localhost:9090/nasdaqStocks", {});
        }
        endpoint<http:HttpClient> nyseEP {
            create http:HttpClient("http://localhost:9090/nyseStocks", {});
        }
        string nyseString = "nyse";
        var nameString = req.getHeader("name");

        http:OutRequest clientRequest = {};
        http:InResponse clientResponse = {};
        http:HttpConnectorError err;
        if (nameString == nyseString) {
            clientResponse, err = nyseEP.post("/stocks", clientRequest);
        } else {
            clientResponse, err = nasdaqEP.post("/stocks", clientRequest);
        }
        _ = conn.forward(clientResponse);
    }
}

@http:configuration {basePath:"/nasdaqStocks"}
service<http> nasdaqStocksQuote {

    @http:resourceConfig {
        methods:["POST"]
    }
    resource stocks (http:Connection conn, http:InRequest req) {
        json payload = {"exchange":"nasdaq", "name":"IBM", "value":"127.50"};
        http:OutResponse res = {};
        res.setJsonPayload(payload);
        _ = conn.respond(res);
    }
}

@http:configuration {basePath:"/nyseStocks"}
service<http> nyseStockQuote {

    @http:resourceConfig {
        methods:["POST"]
    }
    resource stocks (http:Connection con, http:InRequest req) {
        json payload = {"exchange":"nyse", "name":"IBM", "value":"127.50"};
        http:OutResponse res = {};
        res.setJsonPayload(payload);
        _ = con.respond(res);
    }
}