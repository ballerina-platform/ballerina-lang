
import ballerina/http;

@http:configuration {basePath:"/cbr"}
service<http> contentBasedRouting {

    @http:resourceConfig {
        methods:["POST"],
        path:"/"
    }
    resource cbrResource (http:Connection conn, http:Request req) {
        endpoint<http:HttpClient> nasdaqEP {
            create http:HttpClient("http://localhost:9090/nasdaqStocks", {});
        }
        endpoint<http:HttpClient> nyseEP {
            create http:HttpClient("http://localhost:9090/nyseStocks", {});
        }
        string nyseString = "nyse";
        json jsonMsg = req.getJsonPayload();
        var nameString, _ = (string)jsonMsg.name;

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
