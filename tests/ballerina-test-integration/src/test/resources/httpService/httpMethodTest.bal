import ballerina/net.http;

endpoint<http:Service> serviceEnpoint {
    port:9090
}

endpoint<http:Client> endPoint {
    serviceUri : "http://localhost:9090"
}

@http:serviceConfig {
    basePath:"/headQuote",
    endpoints:[serviceEnpoint]
}
service<http:Service> headQuoteService {

    @http:resourceConfig {
        path:"/default"
    }
    resource defaultResource (http:ServerConnector conn, http:Request req) {
        string method = req.method;
        http:Request clientRequest = {};
        http:Response clientResponse = {};
        clientResponse, _ = endPoint -> execute(method, "/getQuote/stocks", clientRequest);
        _ = conn -> forward(clientResponse);
    }

    @http:resourceConfig {
        path:"/forward11"
    }
    resource forwardRes11 (http:ServerConnector conn, http:Request req) {
        http:Response clientResponse = {};
        clientResponse, _ = endPoint -> forward("/getQuote/stocks", req);
        _ = conn -> forward(clientResponse);
    }

    @http:resourceConfig {
        path:"/forward22"
    }
    resource forwardRes22 (http:ServerConnector conn, http:Request req) {
        http:Response clientResponse = {};
        clientResponse, _ = endPoint -> forward("/getQuote/stocks", req);
        _ = conn -> forward(clientResponse);
    }

    @http:resourceConfig {
        path:"/getStock/{method}"
    }
    resource commonResource (http:ServerConnector conn, http:Request req, string method) {
        http:Request clientRequest = {};
        http:Response clientResponse = {};
        clientResponse, _ = endPoint -> execute(method, "/getQuote/stocks", clientRequest);
        _ = conn -> forward(clientResponse);
    }
}

@http:serviceConfig {
    basePath:"/sampleHead",
    endpoints:[serviceEnpoint]
}
service<http:Service> testClientConHEAD {

    @http:resourceConfig {
        methods:["HEAD"],
        path:"/"
    }
    resource passthrough (http:ServerConnector conn, http:Request req) {
        http:Request clientRequest = {};
        http:Response clientResponse = {};
        clientResponse, _ = endPoint -> get("/getQuote/stocks", clientRequest);
        _ = conn -> forward(clientResponse);
    }
}

@http:serviceConfig {
    basePath:"/getQuote",
    endpoints:[serviceEnpoint]
}
service<http:Service> quoteService {

    @http:resourceConfig {
        methods:["GET"],
        path:"/stocks"
    }
    resource company (http:ServerConnector conn, http:Request req) {
        http:Response res = {};
        res.setStringPayload("wso2");
        _ = conn -> respond(res);
    }

    @http:resourceConfig {
        methods:["POST"],
        path:"/stocks"
    }
    resource product (http:ServerConnector conn, http:Request req) {
        http:Response res = {};
        res.setStringPayload("ballerina");
        _ = conn -> respond(res);
    }

    @http:resourceConfig {
        path:"/stocks"
    }
    resource defaultStock (http:ServerConnector conn, http:Request req) {
        http:Response res = {};
        res.setHeader("Method", "any");
        res.setStringPayload("default");
        _ = conn -> respond(res);
    }

    @http:resourceConfig {
        methods:["POST"],
        body:"person"
    }
    resource employee (http:ServerConnector conn, http:Request req, json person) {
        http:Response res = {};
        res.setJsonPayload(person);
        _ = conn -> respond(res);
    }
}
