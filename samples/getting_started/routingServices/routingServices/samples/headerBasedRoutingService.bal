package routingServices.samples;

import ballerina.net.http;

@http:configuration {basePath:"/hbr"}
service<http> headerBasedRouting {

    @http:resourceConfig {
        methods:["GET"],
        path:"/"
    }
    resource hbrResource (http:Request req, http:Response res) {
        http:ClientConnector nasdaqEP = create http:ClientConnector("http://localhost:9090/nasdaqStocks", {});
        http:ClientConnector nyseEP = create http:ClientConnector("http://localhost:9090/nyseStocks", {});
        string nyseString = "nyse";
        string nameString = req.getHeader("name");
        if (nameString == nyseString) {
            res = nyseEP.post("/stocks", req);
        }
        else {
            res = nasdaqEP.post("/stocks", req);
        }
        res.send();
        
    }
    
}
