import ballerina/net.http;

endpoint http:ServiceEndpoint serviceEnpoint {
    port:9090
};

endpoint http:ClientEndpoint endPoint {
    targets: [
        {
            uri: "http://localhost:9090"
        }
    ]
};

@http:ServiceConfig {
    basePath:"/headQuote",
    endpoints:[serviceEnpoint]
}
service<http:Service> headQuoteService {

    @http:ResourceConfig {
        path:"/default"
    }
    defaultResource (endpoint client, http:Request req) {
        string method = req.method;
        http:Request clientRequest = {};
        http:Response clientResponse = {};
        clientResponse =? endPoint -> execute(method, "/getQuote/stocks", clientRequest);
        _ = client -> forward(clientResponse);
    }

    @http:ResourceConfig {
        path:"/forward11"
    }
    forwardRes11 (endpoint client, http:Request req) {
        http:Response clientResponse = {};
        clientResponse =? endPoint -> forward("/getQuote/stocks", req);
        _ = client -> forward(clientResponse);
    }

    @http:ResourceConfig {
        path:"/forward22"
    }
    forwardRes22 (endpoint client, http:Request req) {
        http:Response clientResponse = {};
        clientResponse =? endPoint -> forward("/getQuote/stocks", req);
        _ = client -> forward(clientResponse);
    }

    @http:ResourceConfig {
        path:"/getStock/{method}"
    }
    commonResource (endpoint client, http:Request req, string method) {
        http:Request clientRequest = {};
        http:Response clientResponse = {};
        clientResponse =? endPoint -> execute(method, "/getQuote/stocks", clientRequest);
        _ = client -> forward(clientResponse);
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
    passthrough (endpoint client, http:Request req) {
        http:Request clientRequest = {};
        http:Response clientResponse = {};
        clientResponse =? endPoint -> get("/getQuote/stocks", clientRequest);
        _ = client -> forward(clientResponse);
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
    company (endpoint client, http:Request req) {
        http:Response res = {};
        res.setStringPayload("wso2");
        _ = client -> respond(res);
    }

    @http:ResourceConfig {
        methods:["POST"],
        path:"/stocks"
    }
    product (endpoint client, http:Request req) {
        http:Response res = {};
        res.setStringPayload("ballerina");
        _ = client -> respond(res);
    }

    @http:ResourceConfig {
        path:"/stocks"
    }
    defaultStock (endpoint client, http:Request req) {
        http:Response res = {};
        res.setHeader("Method", "any");
        res.setStringPayload("default");
        _ = client -> respond(res);
    }

    @http:ResourceConfig {
        methods:["POST"],
        body:"person"
    }
    employee (endpoint client, http:Request req, json person) {
        http:Response res = {};
        res.setJsonPayload(person);
        _ = client -> respond(res);
    }
}
