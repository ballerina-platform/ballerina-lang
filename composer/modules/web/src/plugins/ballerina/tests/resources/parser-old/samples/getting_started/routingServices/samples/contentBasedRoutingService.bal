
import ballerina/http;
import ballerina/lang.messages;

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
            response = nyseEP.post("/stocks", m);
        }
        else {
            response = nasdaqEP.post("/stocks", m);
        }
        response:send(response);
        
    }
    
}
