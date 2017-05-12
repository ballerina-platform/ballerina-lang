package routingServices.samples;

import ballerina.net.http;
import ballerina.lang.jsons;
import ballerina.lang.messages;
import ballerina.lang.system;
import ballerina.lang.strings;

@http:BasePath {value:"/cbr"}
service contentBasedRouting {
    
    @http:POST{}
    resource cbrResource (message m) {
        http:ClientConnector nasdaqEP = create http:ClientConnector("http://localhost:9090/nasdaqStocks");
        http:ClientConnector nyseEP = create http:ClientConnector("http://localhost:9090/nyseStocks");
        string nyseString = "nyse";
        json jsonMsg = messages:getJsonPayload(m);
        string nameString = jsons:getString(jsonMsg, "$.name");
	system:log(3,"/cbr resource was invoked with a value of "+nameString+" for $.name json path expression.");
        message response = {};
        if (nameString == nyseString) {
	    system:log(3,"routing to NYSE Service.");
            response = http:ClientConnector.post(nyseEP, "/", m);
            
        }
        else {
	    system:log(3,"routing to NASDAQ Service.");
            response = http:ClientConnector.post(nasdaqEP, "/", m);
        
        }
	json responseJson = messages:getJsonPayload(response);
	string responseString  = jsons:getString(responseJson,"$.exchange");
	string exchangeName = strings:toUpperCase(responseString);
	system:log(3,"Response recieved to /cbr from "+exchangeName+" Service. Responding to client with stock quotes.");
        reply response;
        
    }
    
}
