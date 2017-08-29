import ballerina.net.http;
import ballerina.lang.messages;

@http:configuration {basePath:"/headQuote"}
service<http> headQuoteService {

    @http:resourceConfig {
        path:"/default"
    }
    resource defaultResource (message m) {
        string method = http:getMethod(m);
        http:ClientConnector endPoint = create http:ClientConnector("http://localhost:9090");
        message response = endPoint.execute(method, "/getQuote/stocks", m);
        reply response;
    }

    @http:resourceConfig {
        path:"/getStock/{method}"
    }
    resource commonResource (message m, string method) {
        http:ClientConnector endPoint = create http:ClientConnector("http://localhost:9090");
        message response = endPoint.execute(method, "/getQuote/stocks", m);
        reply response;
    }
}

@http:configuration {basePath:"/sampleHead"}
service<http> testClientConHEAD {

    @http:resourceConfig {
        methods:["HEAD"],
        path:"/"
    }
    resource passthrough (message m) {
        http:ClientConnector quoteEP = create http:ClientConnector("http://localhost:9090");
	    message response = quoteEP.get("/getQuote/stocks", m);
        reply response;
    }
}

@http:configuration {basePath:"/getQuote"}
service<http> quoteService {

    @http:resourceConfig {
        methods:["GET"],
        path:"/stocks"
    }
    resource company (message m) {
        message response = {};
        messages:setStringPayload(response, "wso2");
        reply response;
    }

    @http:resourceConfig {
        methods:["POST"],
        path:"/stocks"
    }
    resource product (message m) {
        message response = {};
        messages:setStringPayload(response, "ballerina");
        reply response;
    }

    @http:resourceConfig {
        path:"/stocks"
    }
    resource defaultStock (message m) {
        message response = {};
        messages:setHeader(response, "Method", "any");
        messages:setStringPayload(response, "default");
        reply response;
    }
}
