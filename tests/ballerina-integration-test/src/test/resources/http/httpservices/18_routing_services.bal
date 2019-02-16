import ballerina/http;
import ballerina/io;
import ballerina/mime;

listener http:Listener serviceEP = new(9114);

http:Client nasdaqEP = new("http://localhost:9114/nasdaqStocks");

http:Client nyseEP2 = new("http://localhost:9114/nyseStocks");

@http:ServiceConfig {basePath:"/cbr"}
service contentBasedRouting on serviceEP {

    @http:ResourceConfig {
        methods:["POST"],
        path:"/"
    }
    resource function cbrResource(http:Caller conn, http:Request req) {
        string nyseString = "nyse";
        var jsonMsg = req.getJsonPayload();
        string nameString = "";
        if (jsonMsg is json) {
            nameString = jsonMsg.name.toString();
        } else {
            io:println("Error getting payload");
        }
        http:Request clientRequest = new;
        http:Response clientResponse = new;
        if (nameString == nyseString) {
            var result = nyseEP2 -> post("/stocks", clientRequest);
            if (result is http:Response) {
                _ = conn -> respond(untaint result);
            } else  {
                clientResponse.statusCode = 500;
                clientResponse.setPayload("Error sending request");
                _ = conn -> respond(clientResponse);
            }
        } else {
            var result = nasdaqEP -> post("/stocks", clientRequest);
            if (result is http:Response) {
                _ = conn -> respond(untaint result);
            } else {
                clientResponse.statusCode = 500;
                clientResponse.setPayload("Error sending request");
                _ = conn -> respond(clientResponse);
            }
        }
    }
}

@http:ServiceConfig {basePath:"/hbr"}
service headerBasedRouting on serviceEP {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/"
    }
    resource function hbrResource(http:Caller caller, http:Request req) {
        string nyseString = "nyse";
        var nameString = req.getHeader("name");

        http:Request clientRequest = new;
        http:Response clientResponse = new;
        if (nameString == nyseString) {
            var result = nyseEP2 -> post("/stocks", clientRequest);
            if (result is http:Response) {
                _ = caller->respond(untaint result);
            } else {
                clientResponse.statusCode = 500;
                clientResponse.setPayload("Error sending request");
                _ = caller->respond(clientResponse);
            }
        } else {
            var result = nasdaqEP -> post("/stocks", clientRequest);
            if (result is http:Response) {
                _ = caller->respond(untaint result);
            } else {
                clientResponse.statusCode = 500;
                clientResponse.setPayload("Error sending request");
                _ = caller->respond(clientResponse);
            }
        }
    }
}

@http:ServiceConfig {basePath:"/nasdaqStocks"}
service nasdaqStocksQuote on serviceEP {

    @http:ResourceConfig {
        methods:["POST"]
    }
    resource function stocks(http:Caller caller, http:Request req) {
        json payload = {"exchange":"nasdaq", "name":"IBM", "value":"127.50"};
        http:Response res = new;
        res.setJsonPayload(payload);
        _ = caller->respond(res);
    }
}

@http:ServiceConfig {basePath:"/nyseStocks"}
service nyseStockQuote2 on serviceEP {

    @http:ResourceConfig {
        methods:["POST"]
    }
    resource function stocks(http:Caller caller, http:Request req) {
        json payload = {"exchange":"nyse", "name":"IBM", "value":"127.50"};
        http:Response res = new;
        res.setJsonPayload(payload);
        _ = caller->respond(res);
    }
}

