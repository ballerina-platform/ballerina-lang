import ballerina.net.http;

@http:config {basePath:"/headQuote"}
service<http> headQuoteService {

    @http:Path{value:"/default"}
    resource defaultResource (message m) {
        string method = http:getMethod(m);
        http:ClientConnector endPoint = create http:ClientConnector("http://localhost:9090");
        message response = http:ClientConnector.execute(endPoint, method, "/getQuote/stocks", m);
        reply response;
    }

    @http:Path{value:"/getStock/{method}"}
    resource commonResource (message m, string method) {
        http:ClientConnector endPoint = create http:ClientConnector("http://localhost:9090");
        message response = http:ClientConnector.execute(endPoint, method, "/getQuote/stocks", m);
        reply response;
    }
}

@http:config {basePath:"/sampleHead"}
service<http> testClientConHEAD {

    @http:HEAD{}
    @http:Path {value:"/"}
    resource passthrough (message m) {
        http:ClientConnector quoteEP = create http:ClientConnector("http://localhost:9090");
	    message response = http:ClientConnector.get(quoteEP, "/getQuote/stocks", m);
        reply response;
    }
}
