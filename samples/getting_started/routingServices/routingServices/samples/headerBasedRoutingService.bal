package routingServices.samples;

import ballerina.net.http;

@http:configuration {basePath:"/hbr"}
service<http> headerBasedRouting {

    @http:resourceConfig {
        methods:["GET"],
        path:"/"
    }
    resource hbrResource (http:Request req, http:Response resp) {
        endpoint<http:HttpClient> nasdaqEP {
            create http:HttpClient("http://localhost:9090/nasdaqStocks", {});
        }
        endpoint<http:HttpClient> nyseEP {
            create http:HttpClient("http://localhost:9090/nyseStocks", {});
        }
        string nyseString = "nyse";
        string nameString;
        boolean headerExists;
        nameString, headerExists = req.getHeader("name");
        http:Response clientResponse = {};
        http:HttpConnectorError err;
        if (headerExists && nameString == nyseString) {
            clientResponse, err = nyseEP.post("/stocks", req);
        } else {
            clientResponse, err = nasdaqEP.post("/stocks", req);
        }
        resp.forward(clientResponse);
    }
}
