import ballerina/http;

endpoint http:Listener serviceEndpoint {
    port:9090
};

endpoint http:Client endPoint {
    url: "http://localhost:9090"
};

@http:ServiceConfig {
    basePath:"/headQuote"
}
service<http:Service> headQuoteService bind serviceEndpoint {

    @http:ResourceConfig {
        path:"/default"
    }
    defaultResource (endpoint client, http:Request req) {
        string method = req.method;
        http:Request clientRequest = new;

        var response = endPoint -> execute(untaint method, "/getQuote/stocks", clientRequest);
        match response {
            http:Response httpResponse => {
                _ = client -> respond(httpResponse);
            }
            http:HttpConnectorError err => {
                http:Response errorResponse = new;
                json errMsg = {"error":"error occurred while invoking the service"};
                errorResponse.setJsonPayload(errMsg);
                _ = client -> respond(errorResponse);
            }
        }
    }

    @http:ResourceConfig {
        path:"/forward11"
    }
    forwardRes11 (endpoint client, http:Request req) {
        var response = endPoint -> forward("/getQuote/stocks", req);
        match response {
            http:Response httpResponse => {
                _ = client -> respond(httpResponse);
            }
            http:HttpConnectorError err => {
                http:Response errorResponse = new;
                json errMsg = {"error":"error occurred while invoking the service"};
                errorResponse.setJsonPayload(errMsg);
                _ = client -> respond(errorResponse);
            }
        }
    }

    @http:ResourceConfig {
        path:"/forward22"
    }
    forwardRes22 (endpoint client, http:Request req) {
        var response = endPoint -> forward("/getQuote/stocks", req);
        match response {
            http:Response httpResponse => {
                _ = client -> respond(httpResponse);
            }
            http:HttpConnectorError err => {
                http:Response errorResponse = new;
                json errMsg = {"error":"error occurred while invoking the service"};
                errorResponse.setJsonPayload(errMsg);
                _ = client -> respond(errorResponse);
            }
        }
    }

    @http:ResourceConfig {
        path:"/getStock/{method}"
    }
    commonResource (endpoint client, http:Request req, string method) {
        http:Request clientRequest = new;
        var response = endPoint -> execute(untaint method, "/getQuote/stocks", clientRequest);
        match response {
            http:Response httpResponse => {
                _ = client -> respond(httpResponse);
            }
            http:HttpConnectorError err => {
                http:Response errorResponse = new;
                json errMsg = {"error":"error occurred while invoking the service"};
                errorResponse.setJsonPayload(errMsg);
                _ = client -> respond(errorResponse);
            }
        }
    }
}

@http:ServiceConfig {
    basePath:"/sampleHead"
}
service<http:Service> testClientConHEAD bind serviceEndpoint {

    @http:ResourceConfig {
        methods:["HEAD"],
        path:"/"
    }
    passthrough (endpoint client, http:Request req) {
        http:Request clientRequest = new;
        var response = endPoint -> get("/getQuote/stocks", request = clientRequest);
        match response {
            http:Response httpResponse => {
                _ = client -> respond(httpResponse);
            }
            http:HttpConnectorError err => {
                http:Response errorResponse = new;
                json errMsg = {"error":"error occurred while invoking the service"};
                errorResponse.setJsonPayload(errMsg);
                _ = client -> respond(errorResponse);
            }
        }
    }
}

@http:ServiceConfig {
    basePath:"/getQuote"
}
service<http:Service> quoteService bind serviceEndpoint {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/stocks"
    }
    company (endpoint client, http:Request req) {
        http:Response res = new;
        res.setStringPayload("wso2");
        _ = client -> respond(res);
    }

    @http:ResourceConfig {
        methods:["POST"],
        path:"/stocks"
    }
    product (endpoint client, http:Request req) {
        http:Response res = new;
        res.setStringPayload("ballerina");
        _ = client -> respond(res);
    }

    @http:ResourceConfig {
        path:"/stocks"
    }
    defaultStock (endpoint client, http:Request req) {
        http:Response res = new;
        res.setHeader("Method", "any");
        res.setStringPayload("default");
        _ = client -> respond(res);
    }

    @http:ResourceConfig {
        methods:["POST"],
        body:"person"
    }
    employee (endpoint client, http:Request req, json person) {
        http:Response res = new;
        res.setJsonPayload(person);
        _ = client -> respond(res);
    }
}
