import ballerina.net.http;
import ballerina.doc;
import ballerina.net.http.request;
import ballerina.net.http.response;

@doc:Description {value:"Service is invoke using BasePath value (/hbr)."}
@http:configuration {basePath:"/hbr"}
service<http> headerBasedRouting {
    @doc:Description {value:"The http:resourceConfig{} annotation with GET method declares the HTTP method."}
    @http:resourceConfig {
        methods:["GET"],
        path:"/route"
    }
    resource hbrResource (http:Request req, http:Response res) {
        http:ClientConnector locationEP;
        http:ClientConnector weatherEP;
        //Create two service endpoints using HTTP client-connector with different hosts.
        locationEP = create http:ClientConnector("http://www.mocky.io", {});
        weatherEP = create http:ClientConnector("http://samples.openweathermap.org", {});
        //Native function getHeader() returns header value of a specified header name.
        string nameString = request:getHeader(req, "type");

        if (nameString == "location") {
            //"post" represent the POST action of HTTP connector. Route payload to relevant service.
            res = locationEP.post("/v2/594e12271100001f13d6d3a6", req);
        } else {
            //"get" action can be used to make http GET call.
            http:Request newRequest = {};
            res = weatherEP.get("/data/2.5/weather?lat=35&lon=139&appid=b1b1",
                                newRequest);
        }
        response:send(res);
    }
}
