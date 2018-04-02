package routingServices.samples;

import ballerina/http;
import ballerina/lang.messages;

@http:configuration {basePath:"/hbr"}
service<http> headerBasedRouting {

    @http:resourceConfig {
        methods:["GET"],
        path:"/"
    }
    resource hbrResource (message m) {
        http:ClientConnector nasdaqEP = create http:ClientConnector("http://localhost:9090/nasdaqStocks");
        http:ClientConnector nyseEP = create http:ClientConnector("http://localhost:9090/nyseStocks");
        string nyseString = "nyse";
        string nameString = messages:getHeader(m, "name");
        message response = {};
        if (nameString == nyseString) {
            response = nyseEP.post("/stocks", m);
        }
        else {
            response = nasdaqEP.post("/stocks", m);
        }
        response:send(response);
        
    }
    
}
