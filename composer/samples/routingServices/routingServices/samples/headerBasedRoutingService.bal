package routingServices.samples;

import ballerina/http;

@http:configuration {basePath:"/hbr"}
service<http> headerBasedRouting {

    @http:resourceConfig {
        methods:["GET"],
        path:"/"
    }
    resource hbrResource (http:Connection conn, http:Request req) {
        endpoint<http:HttpClient> nasdaqEP {
            create http:HttpClient("http://localhost:9090/nasdaqStocks", {});
        }
        endpoint<http:HttpClient> nyseEP {
            create http:HttpClient("http://localhost:9090/nyseStocks", {});
        }
        string nyseString = "nyse";
        var nameString = req.getHeader("name");

        http:Request clientRequest = {};
        http:Response clientResponse = {};
        http:HttpConnectorError err;
        if (nameString == nyseString) {
            clientResponse, err = nyseEP.post("/stocks", clientRequest);
        } else {
            clientResponse, err = nasdaqEP.post("/stocks", clientRequest);
        }
        _ = conn.forward(clientResponse);
    }
}
