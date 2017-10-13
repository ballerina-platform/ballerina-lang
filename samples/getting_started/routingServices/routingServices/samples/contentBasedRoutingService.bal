package routingServices.samples;

import ballerina.net.http;

@http:configuration {basePath:"/cbr"}
service<http> contentBasedRouting {

    @http:resourceConfig {
        methods:["POST"],
        path:"/"
    }
    resource cbrResource (http:Request req, http:Response res) {
        http:ClientConnector nasdaqEP = create http:ClientConnector("http://localhost:9090/nasdaqStocks", {});
        http:ClientConnector nyseEP = create http:ClientConnector("http://localhost:9090/nyseStocks", {});
        string nyseString = "nyse";
        json jsonMsg = req.getJsonPayload();
        var nameString, _ = (string) jsonMsg.name;
        if (nameString == nyseString) {
            res = nyseEP.post("/stocks", req);
        }
        else {
            res = nasdaqEP.post("/stocks", req);
        }
        res.send();

    }
    
}
