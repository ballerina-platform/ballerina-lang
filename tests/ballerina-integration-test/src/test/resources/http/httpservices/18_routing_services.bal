import ballerina/http;
import ballerina/io;
import ballerina/mime;

endpoint http:Listener serviceEP {
    port:9114
};

endpoint http:Client nasdaqEP {
    url:"http://localhost:9114/nasdaqStocks"
};

endpoint http:Client nyseEP2 {
    url:"http://localhost:9114/nyseStocks"
};

@http:ServiceConfig {basePath:"/cbr"}
service<http:Service> contentBasedRouting bind serviceEP{

    @http:ResourceConfig {
        methods:["POST"],
        path:"/"
    }
    cbrResource (endpoint conn, http:Request req) {
        string nyseString = "nyse";
        var jsonMsg = req.getJsonPayload();
        string nameString = "";
        if (jsonMsg is json) {
            nameString = extractFieldValue1(jsonMsg.name);
        } else if (jsonMsg is error) {
            io:println("Error getting payload");
        }
        http:Request clientRequest = new;
        http:Response clientResponse = new;
        if (nameString == nyseString) {
            var result = nyseEP2 -> post("/stocks", clientRequest);
            if (result is http:Response) {
                _ = conn -> respond(untaint result);
            } else if (result is error) {
                clientResponse.statusCode = 500;
                clientResponse.setPayload("Error sending request");
                _ = conn -> respond(clientResponse);
            }
        } else {
            var result = nasdaqEP -> post("/stocks", clientRequest);
            if (result is http:Response) {
                _ = conn -> respond(untaint result);
            } else if (result is error) {
                clientResponse.statusCode = 500;
                clientResponse.setPayload("Error sending request");
                _ = conn -> respond(clientResponse);
            }
        }
    }
}

@http:ServiceConfig {basePath:"/hbr"}
service<http:Service> headerBasedRouting bind serviceEP{

    @http:ResourceConfig {
        methods:["GET"],
        path:"/"
    }
    hbrResource (endpoint conn, http:Request req) {
        string nyseString = "nyse";
        var nameString = req.getHeader("name");

        http:Request clientRequest = new;
        http:Response clientResponse = new;
        if (nameString == nyseString) {
            var result = nyseEP2 -> post("/stocks", clientRequest);
            if (result is http:Response) {
                _ = conn -> respond(untaint result);
            } else if (result is error) {
                clientResponse.statusCode = 500;
                clientResponse.setPayload("Error sending request");
                _ = conn -> respond(clientResponse);
            }
        } else {
            var result = nasdaqEP -> post("/stocks", clientRequest);
            if (result is http:Response) {
                _ = conn -> respond(untaint result);
            } else if (result is error) {
                clientResponse.statusCode = 500;
                clientResponse.setPayload("Error sending request");
                _ = conn -> respond(clientResponse);
            }
        }
    }
}

@http:ServiceConfig {basePath:"/nasdaqStocks"}
service<http:Service> nasdaqStocksQuote bind serviceEP {

    @http:ResourceConfig {
        methods:["POST"]
    }
    stocks (endpoint conn, http:Request req) {
        json payload = {"exchange":"nasdaq", "name":"IBM", "value":"127.50"};
        http:Response res = new;
        res.setJsonPayload(payload);
        _ = conn -> respond(res);
    }
}

@http:ServiceConfig {basePath:"/nyseStocks"}
service<http:Service> nyseStockQuote2 bind serviceEP {

    @http:ResourceConfig {
        methods:["POST"]
    }
    stocks (endpoint conn, http:Request req) {
        json payload = {"exchange":"nyse", "name":"IBM", "value":"127.50"};
        http:Response res = new;
        res.setJsonPayload(payload);
        _ = conn -> respond(res);
    }
}

//Keep this until there's a simpler way to get a string value out of a json
function extractFieldValue1(json fieldValue) returns string {
    match fieldValue {
        int i => return "error";
        string s => return s;
        boolean b => return "error";
        ()  => return "error";
        json j => return "error";
    }
}
