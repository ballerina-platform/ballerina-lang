package routingServices.samples;

import ballerina.net.http;
import ballerina.lang.jsons;
import ballerina.lang.messages;

@http:BasePath {value:"/cbr"}
service contentBasedRouting {
    
    @http:POST{}
    @http:Path {value:"/"}
    resource cbrResource (message m) {
        http:ClientConnector nasdaqEP = create http:ClientConnector("http://localhost:9090/nasdaqStocks");
        http:ClientConnector nyseEP = create http:ClientConnector("http://localhost:9090/nyseStocks");
        string nyseString = "nyse";
        json jsonMsg = messages:getJsonPayload(m);
        string nameString = jsons:getString(jsonMsg, "$.name");
        message response = {};
        if (nameString == nyseString) {
            response = http:ClientConnector.post(nyseEP, "/stocks", m);
            
        }
        else {
            response = http:ClientConnector.post(nasdaqEP, "/stocks", m);
        
        }
        reply response;
        
    }
    
}
