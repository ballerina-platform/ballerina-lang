import ballerina.net.http;
import ballerina.lang.messages;
import ballerina.doc;

@doc:Description {value:"Service is invoke using BasePath value (/hbr)."}
@http:configuration {basePath:"/hbr"}
service<http> headerBasedRouting {
    @doc:Description {value:"http:GET{} annotation declares the HTTP method."}
    @http:resourceConfig {
        methods:["GET"],
        path:"/route"
    }
    resource hbrResource (message m) {
        //Create two service endpoints using HTTP client-connector with different hosts.
        http:ClientConnector locationEP = create http:ClientConnector(
                                          "http://www.mocky.io");
        http:ClientConnector weatherEP = create http:ClientConnector(
                                         "http://samples.openweathermap.org");
        //Native function getHeader() returns header value of a specified header name.
        string nameString = messages:getHeader(m, "type");

        message response = {};
        if (nameString == "location") {
        //"post" represent the POST action of HTTP connector. Route payload to relevant service.
            response = locationEP.post("/v2/594e12271100001f13d6d3a6", m);
        } else {
        //"get" action can be used to make http GET call.
            message newMessage = {};
            response = weatherEP.get
              ("/data/2.5/weather?lat=35&lon=139&appid=b1b1", newMessage);
        }
        reply response;
    }
}
