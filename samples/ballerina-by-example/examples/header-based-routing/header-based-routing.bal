import ballerina.net.http;

@Description {value:"Service is invoke using BasePath value (/hbr)."}
@http:configuration {basePath:"/hbr"}
service<http> headerBasedRouting {
    @Description {value:"The http:resourceConfig{} annotation with GET method declares the HTTP method."}
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
        //Create new request and response to handle client call.
        http:Request newRequest = {};
        http:Response clientResponse = {};
        //Native function getHeader() returns header value of a specified header name.
        string nameString = req.getHeader("type");
        if (nameString == "location") {
            //"post" represent the POST action of HTTP connector. Route payload to relevant service.
            clientResponse = locationEP.post("/v2/594e12271100001f13d6d3a6", newRequest);
        } else {
            //"get" action can be used to make http GET call.

            clientResponse = weatherEP.get("/data/2.5/weather?lat=35&lon=139&appid=b1b1", newRequest);
        }
        //Native function "forward" sends back the clientResponse to the caller.
        res.forward(clientResponse);
    }
}
