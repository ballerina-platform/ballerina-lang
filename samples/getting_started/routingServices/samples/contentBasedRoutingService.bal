package routingServices.samples;

import ballerina.net.http;
import ballerina.lang.messages;

@http:configuration {basePath:"/cbr"}
service<http> contentBasedRouting {

    @http:resourceConfig {
        methods:["POST"],
        path:"/"
    }
    resource cbrResource (message m) {
        http:ClientConnector nasdaqEP = create http:ClientConnector("http://localhost:9090/nasdaqStocks");
        http:ClientConnector nyseEP = create http:ClientConnector("http://localhost:9090/nyseStocks");
        string nyseString = "nyse";
        json jsonMsg = messages:getJsonPayload(m);
        var nameString, _ = (string) jsonMsg.name;
        message response = {};
        if (nameString == nyseString) {
            response = http:ClientConnector.post(nyseEP, "/stocks", m);
            
        } else {
            response = http:ClientConnector.post(nasdaqEP, "/stocks", m);
        
        }
        reply response;
        
    }
    
}
