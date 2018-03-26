import ballerina/io;
import ballerina/mime;
import ballerina/net.http;

endpoint http:ServiceEndpoint serviceEnpoint {
    port:9090
};

endpoint http:ClientEndpoint nasdaqEP {
    targets: [{uri: "http://localhost:9090/nasdaqStocks"}]
};

endpoint http:ClientEndpoint nyseEP {
    targets: [{uri: "http://localhost:9090/nyseStocks"}]
};

@http:ServiceConfig {
    basePath:"/cbr"
}
service<http:Service> contentBasedRouting bind serviceEnpoint {

    @http:ResourceConfig {
        methods:["POST"],
        path:"/"
    }
    cbrResource (endpoint outboundEP, http:Request req) {
        string nyseString = "nyse";
        string nameString;
        var jsonMsg = req.getJsonPayload();
        match jsonMsg {
            json msg => {
                nameString =? <string>msg.name;
            }
            mime:EntityError err => {
                io:println("Error occurred while reading name string");
                return;
            }
        }
        http:Request clientRequest = {};
        http:Response clientResponse = {};
        if (nameString == nyseString) {
            var clientRes = nyseEP -> post("/stocks", clientRequest);
            match clientRes {
                http:Response stock => {
                    clientResponse = stock;
                }
                http:HttpConnectorError err => {
                    io:println("Error occurred while writing nyse stocks response");
                    return;
                }
            }
        } else {
            var clientRes = nasdaqEP -> post("/stocks", clientRequest);
            match clientRes {
                http:Response stock => {
                    clientResponse = stock;
                }
                http:HttpConnectorError err => {
                    io:println("Error occurred while writing nasdaq stocks response");
                    return;
                }
            }
        }
        _ = outboundEP -> forward(clientResponse);
    }
}

@http:ServiceConfig {
    basePath:"/hbr"
}
service<http:Service> headerBasedRouting bind serviceEnpoint {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/"
    }
    hbrResource (endpoint outboundEP, http:Request req) {
        string nyseString = "nyse";
        var nameString = req.getHeader("name");

        http:Request clientRequest = {};
        http:Response clientResponse = {};
        if (nameString == nyseString) {
            var clientRes = nyseEP -> post("/stocks", clientRequest);
                match clientRes {
                    http:Response stock => {
                    clientResponse = stock;
                }
                http:HttpConnectorError err => {
                    io:println("Error occurred while writing nyse stocks response");
                    return;
                }
            }
        } else {
            var clientRes = nasdaqEP -> post("/stocks", clientRequest);
            match clientRes {
                http:Response stock => {
                    clientResponse = stock;
                }
                http:HttpConnectorError err => {
                    io:println("Error occurred while writing nasdaq stocks response");
                    return;
                }
            }
        }
        _ = outboundEP -> forward(clientResponse);
    }
}

@http:ServiceConfig {
    basePath:"/nasdaqStocks"
}
service<http:Service> nasdaqStocksQuote bind serviceEnpoint{

    @http:ResourceConfig {
        methods:["POST"]
    }
    stocks (endpoint outboundEP, http:Request req) {
        json payload = {"exchange":"nasdaq", "name":"IBM", "value":"127.50"};
        http:Response res = {};
        res.setJsonPayload(payload);
        _ = outboundEP -> respond(res);
    }
}

@http:ServiceConfig {
    basePath:"/nyseStocks"
}
service<http:Service> nyseStockQuote bind serviceEnpoint {

    @http:ResourceConfig {
        methods:["POST"]
    }
    stocks (endpoint outboundEP, http:Request req) {
        json payload = {"exchange":"nyse", "name":"IBM", "value":"127.50"};
        http:Response res = {};
        res.setJsonPayload(payload);
        _ = outboundEP -> respond(res);
    }
}