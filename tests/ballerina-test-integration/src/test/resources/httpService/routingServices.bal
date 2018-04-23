import ballerina/http;
import ballerina/io;
import ballerina/mime;

endpoint http:Listener serviceEP {
    port:9090
};

endpoint http:Client nasdaqEP {
    url:"http://localhost:9090/nasdaqStocks"
};

endpoint http:Client nyseEP {
    url:"http://localhost:9090/nyseStocks"
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
        string nameString;
        match jsonMsg {
            error payloadError => io:println("Error getting payload");
            json payload =>  {
                nameString = extractFieldValue(payload.name);
            }
        }
        http:Request clientRequest = new;
        http:Response clientResponse = new;
        if (nameString == nyseString) {
            var result = nyseEP -> post("/stocks", request = clientRequest);
            match result {
                http:HttpConnectorError err => {
                    clientResponse.statusCode = 500;
                    clientResponse.setTextPayload("Error sending request");
                    _ = conn -> respond(clientResponse);
                }
                http:Response returnResponse => _ = conn -> respond(returnResponse);
            }
        } else {
            var result = nasdaqEP -> post("/stocks", request = clientRequest);
            match result {
                http:HttpConnectorError err => {
                    clientResponse.statusCode = 500;
                    clientResponse.setTextPayload("Error sending request");
                    _ = conn -> respond(clientResponse);
                }
                http:Response returnResponse => _ = conn -> respond(returnResponse);
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
            var result = nyseEP -> post("/stocks", request = clientRequest);
            match result {
                http:HttpConnectorError err => {
                    clientResponse.statusCode = 500;
                    clientResponse.setTextPayload("Error sending request");
                    _ = conn -> respond(clientResponse);
                }
                http:Response returnResponse => _ = conn -> respond(returnResponse);
            }
        } else {
            var result = nasdaqEP -> post("/stocks", request = clientRequest);
            match result {
                http:HttpConnectorError err => {
                    clientResponse.statusCode = 500;
                    clientResponse.setTextPayload("Error sending request");
                    _ = conn -> respond(clientResponse);
                }
                http:Response returnResponse => _ = conn -> respond(returnResponse);
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
service<http:Service> nyseStockQuote bind serviceEP {

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
function extractFieldValue(json fieldValue) returns string {
    match fieldValue {
        int i => return "error";
        string s => return s;
        boolean b => return "error";
        ()  => return "error";
        json j => return "error";
    }
}
