import ballerina/http;
import ballerina/mime;

endpoint http:Listener passthroughEP1 {
    port: 9113
};

endpoint http:Client nyseEP1 {
    url: "http://localhost:9113"
};

@http:ServiceConfig { basePath: "/passthrough" }
service<http:Service> passthroughService bind passthroughEP1 {

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/"
    }
    passthrough(endpoint caller, http:Request clientRequest) {
        var response = nyseEP1->get("/nyseStock/stocks", message = untaint clientRequest);
        match response {
            http:Response httpResponse => {
                _ = caller->respond(httpResponse);
            }
            error err => {
                _ = caller->respond({ "error": "error occurred while invoking the service" });
            }
        }
    }

    @http:ResourceConfig {
        methods: ["POST"],
        path: "/forwardMultipart"
    }
    forwardMultipart(endpoint caller, http:Request clientRequest) {
        var response = nyseEP1->forward("/nyseStock/stocksAsMultiparts", clientRequest);
        match response {
            http:Response httpResponse => {
                _ = caller->respond(httpResponse);
            }
            error err => {
                _ = caller->respond({ "error": "error occurred while invoking the service" });
            }
        }
    }
}

@http:ServiceConfig { basePath: "/nyseStock" }
service<http:Service> nyseStockQuote1 bind passthroughEP1 {

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/stocks"
    }
    stocks(endpoint caller, http:Request clientRequest) {
        _ = caller->respond({ "exchange": "nyse", "name": "IBM", "value": "127.50" });
    }

    @http:ResourceConfig {
        methods: ["POST"],
        path: "/stocksAsMultiparts"
    }
    stocksAsMultiparts(endpoint caller, http:Request clientRequest) {
        mime:Entity[] bodyParts = check clientRequest.getBodyParts();
        _ = caller->respond(untaint bodyParts);
    }
}
