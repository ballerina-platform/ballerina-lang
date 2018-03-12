import ballerina.net.http;

@http:configuration {basePath:"/headQuote"}
service<http> headQuoteService {

    @http:resourceConfig {
        path:"/default"
    }
    resource defaultResource (http:Connection conn, http:Request req) {
        endpoint<http:HttpClient> endPoint {
            create http:HttpClient("http://localhost:9090", {});
        }
        string method = req.method;
        http:Request clientRequest = {};
        http:Response clientResponse = {};
        clientResponse, _ = endPoint.execute(method, "/getQuote/stocks", clientRequest);
        _ = conn.forward(clientResponse);
    }

    @http:resourceConfig {
        path:"/forward11"
    }
    resource forwardRes11 (http:Connection conn, http:Request req) {
        endpoint<http:HttpClient> endPoint {
              create http:HttpClient("http://localhost:9090", {});
        }
        http:Response clientResponse = {};
        clientResponse, _ = endPoint.forward("/getQuote/stocks", req);
        _ = conn.forward(clientResponse);
    }

    @http:resourceConfig {
        path:"/forward22"
    }
    resource forwardRes22 (http:Connection conn, http:Request req) {
        endpoint<http:HttpClient> endPoint {
              create http:HttpClient("http://localhost:9090", {});
        }
        http:Response clientResponse = {};
        clientResponse, _ = endPoint.forward("/getQuote/stocks", req);
        _ = conn.forward(clientResponse);
    }

    @http:resourceConfig {
        path:"/getStock/{method}"
    }
    resource commonResource (http:Connection conn, http:Request req, string method) {
        endpoint<http:HttpClient> endPoint {
            create http:HttpClient("http://localhost:9090", {});
        }
        http:Request clientRequest = {};
        http:Response clientResponse = {};
        clientResponse, _ = endPoint.execute(method, "/getQuote/stocks", clientRequest);
        _ = conn.forward(clientResponse);
    }
}

@http:configuration {basePath:"/sampleHead"}
service<http> testClientConHEAD {

    @http:resourceConfig {
        methods:["HEAD"],
        path:"/"
    }
    resource passthrough (http:Connection conn, http:Request req) {
        endpoint<http:HttpClient> quoteEP {
            create http:HttpClient("http://localhost:9090", {});
        }
        http:Request clientRequest = {};
        http:Response clientResponse = {};
        clientResponse, _ = quoteEP.get("/getQuote/stocks", clientRequest);
        _ = conn.forward(clientResponse);
    }
}

@http:configuration {basePath:"/getQuote"}
service<http> quoteService {

    @http:resourceConfig {
        methods:["GET"],
        path:"/stocks"
    }
    resource company (http:Connection conn, http:Request req) {
        http:Response res = {};
        res.setStringPayload("wso2");
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        methods:["POST"],
        path:"/stocks"
    }
    resource product (http:Connection conn, http:Request req) {
        http:Response res = {};
        res.setStringPayload("ballerina");
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        path:"/stocks"
    }
    resource defaultStock (http:Connection conn, http:Request req) {
        http:Response res = {};
        res.setHeader("Method", "any");
        res.setStringPayload("default");
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        methods:["POST"],
        body:"person"
    }
    resource employee (http:Connection conn, http:Request req, json person) {
        http:Response res = {};
        res.setJsonPayload(person);
        _ = conn.respond(res);
    }
}
