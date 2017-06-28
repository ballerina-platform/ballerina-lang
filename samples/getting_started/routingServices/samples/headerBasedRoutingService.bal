package routingServices.samples;

import ballerina.net.http;
import ballerina.lang.messages;

@http:BasePath {value:"/hbr"}
service headerBasedRouting {
    
    @http:GET{}
    @http:Path {value:"/"}
    resource cbrResource (message m) {
        //this service routes the request to a backend based on a value sent in as a HTTP header.
        http:ClientConnector nasdaqEP = create http:ClientConnector("http://localhost:9090/nasdaqStocks");
        http:ClientConnector nyseEP = create http:ClientConnector("http://localhost:9090/nyseStocks");
        string nyseString = "nyse";
        //getHeader is a function in the messages package that extracts a header value based on header name provided to it.
        string nameString = messages:getHeader(m, "name");
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