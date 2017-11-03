import ballerina.net.http;

@http:configuration {basePath:"/headQuote"}
service<http> headQuoteService {

    @http:resourceConfig {
        path:"/default"
    }
    resource defaultResource (http:Request req, http:Response resp) {
        endpoint<http:ClientConnector> endPoint {
            create http:ClientConnector("http://localhost:9090", {});
        }
        string method = req.getMethod();
        http:Response clientResponse;
        clientResponse, _ = endPoint.execute(method, "/getQuote/stocks", req);
        resp.forward(clientResponse);
    }

    @http:resourceConfig {
        path:"/getStock/{method}"
    }
    resource commonResource (http:Request req, http:Response resp, string method) {
        endpoint<http:ClientConnector> endPoint {
            create http:ClientConnector("http://localhost:9090", {});
        }
        http:Response clientResponse;
        clientResponse, _ = endPoint.execute(method, "/getQuote/stocks", req);
        resp.forward(clientResponse);
    }
}

@http:configuration {basePath:"/sampleHead"}
service<http> testClientConHEAD {

    @http:resourceConfig {
        methods:["HEAD"],
        path:"/"
    }
    resource passthrough (http:Request req, http:Response resp) {
        endpoint<http:ClientConnector> quoteEP {
            create http:ClientConnector("http://localhost:9090", {});
        }
        http:Response clientResponse;
        clientResponse, _ = quoteEP.get("/getQuote/stocks", req);
        resp.forward(clientResponse);
    }
}

@http:configuration {basePath:"/getQuote"}
service<http> quoteService {

    @http:resourceConfig {
        methods:["GET"],
        path:"/stocks"
    }
    resource company (http:Request req, http:Response res) {
        res.setStringPayload("wso2");
        res.send();
    }

    @http:resourceConfig {
        methods:["POST"],
        path:"/stocks"
    }
    resource product (http:Request req, http:Response res) {
        res.setStringPayload("ballerina");
        res.send();
    }

    @http:resourceConfig {
        path:"/stocks"
    }
    resource defaultStock (http:Request req, http:Response res) {
        res.setHeader("Method", "any");
        res.setStringPayload("default");
        res.send();
    }
}
