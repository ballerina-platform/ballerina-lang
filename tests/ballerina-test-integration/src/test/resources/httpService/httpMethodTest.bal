import ballerina/net.http;

endpoint http:ServiceEndpoint serviceEnpoint {
    port:9090
};

endpoint http:ClientEndpoint endPoint {
    serviceUri : "http://localhost:9090"
};

@http:ServiceConfig {
    basePath:"/headQuote",
    endpoints:[serviceEnpoint]
}
service<http:Service> headQuoteService {

    @http:ResourceConfig {
        path:"/default"
    }
    defaultResource (http:ServerConnector conn, http:Request req) {
        string method = req.method;
        http:Request clientRequest = {};
        http:Response clientResponse = {};
        clientResponse, _ = endPoint -> execute(method, "/getQuote/stocks", clientRequest);
        _ = conn -> forward(clientResponse);
    }

    @http:ResourceConfig {
        path:"/forward11"
    }
    forwardRes11 (http:ServerConnector conn, http:Request req) {
        http:Response clientResponse = {};
        clientResponse, _ = endPoint -> forward("/getQuote/stocks", req);
        _ = conn -> forward(clientResponse);
    }

    @http:ResourceConfig {
        path:"/forward22"
    }
    forwardRes22 (http:ServerConnector conn, http:Request req) {
        http:Response clientResponse = {};
        clientResponse, _ = endPoint -> forward("/getQuote/stocks", req);
        _ = conn -> forward(clientResponse);
    }

    @http:ResourceConfig {
        path:"/getStock/{method}"
    }
    commonResource (http:ServerConnector conn, http:Request req, string method) {
        http:Request clientRequest = {};
        http:Response clientResponse = {};
        clientResponse, _ = endPoint -> execute(method, "/getQuote/stocks", clientRequest);
        _ = conn -> forward(clientResponse);
    }
}

@http:ServiceConfig {
    basePath:"/sampleHead",
    endpoints:[serviceEnpoint]
}
service<http:Service> testClientConHEAD {

    @http:ResourceConfig {
        methods:["HEAD"],
        path:"/"
    }
    passthrough (http:ServerConnector conn, http:Request req) {
        http:Request clientRequest = {};
        http:Response clientResponse = {};
        clientResponse, _ = endPoint -> get("/getQuote/stocks", clientRequest);
        _ = conn -> forward(clientResponse);
    }
}

@http:ServiceConfig {
    basePath:"/getQuote",
    endpoints:[serviceEnpoint]
}
service<http:Service> quoteService {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/stocks"
    }
    company (http:ServerConnector conn, http:Request req) {
        http:Response res = {};
        res.setStringPayload("wso2");
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["POST"],
        path:"/stocks"
    }
    product (http:ServerConnector conn, http:Request req) {
        http:Response res = {};
        res.setStringPayload("ballerina");
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        path:"/stocks"
    }
    defaultStock (http:ServerConnector conn, http:Request req) {
        http:Response res = {};
        res.setHeader("Method", "any");
        res.setStringPayload("default");
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["POST"],
        body:"person"
    }
    employee (http:ServerConnector conn, http:Request req, json person) {
        http:Response res = {};
        res.setJsonPayload(person);
        _ = conn -> respond(res);
    }
}
