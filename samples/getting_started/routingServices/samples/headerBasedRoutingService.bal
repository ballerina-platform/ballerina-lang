package routingServices.samples;
import ballerina.net.http;
import ballerina.lang.messages;

@http:BasePath ("/hbr")
service headerBasedRouting {
    
    @http:GET
    resource cbrResource (message m) {
        http:ClientConnector nasdaqEP = create http:ClientConnector("http://localhost:9090/nasdaqStocks");
        http:ClientConnector nyseEP = create http:ClientConnector("http://localhost:9090/nyseStocks");
        string nyseString = "nyse";
        string nameString = messages:getHeader(m, "name");
        message response = {};
        if (nameString == nyseString) {
            response = http:ClientConnector.post(nyseEP, "/", m);
            
        }
        else {
            response = http:ClientConnector.post(nasdaqEP, "/", m);
            
        }
        reply response;
        
    }
    
}