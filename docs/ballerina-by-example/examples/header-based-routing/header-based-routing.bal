import ballerina.net.http;

@Description {value:"Attributes associated with the service endpoint is defined here."}
endpoint<http:Service> hbrEP {
    port:9090
}

endpoint<http:Client> locationEP {
    serviceUri: "http://www.mocky.io"
}

endpoint<http:Client> weatherEP {
    serviceUri: "http://samples.openweathermap.org"
}

@Description {value:"Service is invoke using BasePath value (/hbr)."}
@http:serviceConfig { basePath:"/hbr", endpoints:[hbrEP] }
service<http:Service> headerBasedRouting {
    @Description {value:"The http:resourceConfig{} annotation with GET method declares the HTTP method."}
    @http:resourceConfig {
        methods:["GET"],
        path:"/route"
    }
    resource hbrResource (http:ServerConnector conn, http:Request req) {
        //Create new outbound request and inbound response to handle client call.
        http:Request newRequest = {};
        http:Response clientResponse = {};
        http:HttpConnectorError err;
        //Native function getHeader() returns header value of a specified header name.
        string nameString = req.getHeader("type");
        if (nameString == "location") {
            //"post" represent the POST action of HTTP connector. Route payload to relevant service.
            clientResponse, err = locationEP -> post("/v2/594e12271100001f13d6d3a6", newRequest);
        } else {
            //"get" action can be used to make http GET call.
            clientResponse, err = weatherEP -> get("/data/2.5/weather?lat=35&lon=139&appid=b1b1", newRequest);
        }

        //Native function "forward" sends back the inbound clientResponse to the caller if no any error is found.
        http:Response res = {};
        if (err != null) {
            res.statusCode = 500;
            res.setStringPayload(err.message);
            _ = conn -> respond(res);
        } else {
            _ = conn -> forward(clientResponse);
        }
    }
}
