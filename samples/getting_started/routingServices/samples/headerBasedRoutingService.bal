package routingServices.samples;

import ballerina.net.http;
import ballerina.lang.jsons;
import ballerina.lang.messages;
import ballerina.lang.strings;
import ballerina.lang.system;

@http:BasePath {value:"/hbr"}
service headerBasedRouting {
    
    @http:GET{}
    resource cbrResource (message m) {
        http:ClientConnector nasdaqEP = create http:ClientConnector("http://localhost:9090/nasdaqStocks");
        http:ClientConnector nyseEP = create http:ClientConnector("http://localhost:9090/nyseStocks");
        string nyseString = "nyse";
        string nameString = messages:getHeader(m, "name");
	int length=strings:length(nameString);
	system:log(3,"/hbr resource was invoked with a name header value of "+nameString+".");
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
