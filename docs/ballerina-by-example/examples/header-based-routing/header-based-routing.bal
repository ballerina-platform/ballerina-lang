import ballerina.net.http;

@Description {value:"Service is invoke using BasePath value (/hbr)."}
@http:configuration {basePath:"/hbr"}
service<http> headerBasedRouting {
    @Description {value:"The http:resourceConfig{} annotation with GET method declares the HTTP method."}
    @http:resourceConfig {
        methods:["GET"],
        path:"/route"
    }
    resource hbrResource (http:Connection conn, http:InRequest req) {
        endpoint<http:HttpClient> locationEP {
            create http:HttpClient("http://www.mocky.io", {});
        }
        endpoint<http:HttpClient> weatherEP {
            create http:HttpClient("http://samples.openweathermap.org", {});
        }
        //Create new outbound request and inbound response to handle client call.
        http:OutRequest newRequest = {};
        http:InResponse clientResponse = {};
        http:HttpConnectorError err;
        //Native function getHeader() returns header value of a specified header name.
        string nameString = req.getHeader("type");
        if (nameString == "location") {
            //"post" represent the POST action of HTTP connector. Route payload to relevant service.
            clientResponse, err = locationEP.post("/v2/594e12271100001f13d6d3a6", newRequest);
        } else {
            //"get" action can be used to make http GET call.
            clientResponse, err = weatherEP.get("/data/2.5/weather?lat=35&lon=139&appid=b1b1", newRequest);
        }

        //Native function "forward" sends back the inbound clientResponse to the caller if no any error is found.
        http:OutResponse res = {};
        if (err != null) {
            res.statusCode = 500;
            res.setStringPayload(err.message);
            _ = conn.respond(res);
        } else {
            _ = conn.forward(clientResponse);
        }
    }
}
