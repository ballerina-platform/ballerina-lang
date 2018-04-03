import ballerina/http;

@Description {value:"Attributes associated with the service endpoint is defined here."}
endpoint http:ServiceEndpoint hbrEP {
    port:9090
};

endpoint http:ClientEndpoint locationEP {
    targets: [
        {
            uri: "http://www.mocky.io"
        }
    ]
};

endpoint http:ClientEndpoint weatherEP {
    targets: [
        {
            uri: "http://samples.openweathermap.org"
        }
    ]
};

@Description {value:"Service is invoke using BasePath value (/hbr)."}
@http:ServiceConfig {
    basePath:"/hbr"
}
service<http:Service> headerBasedRouting bind hbrEP {
    @Description {value:"The http:resourceConfig{} annotation with GET method declares the HTTP method."}
    @http:ResourceConfig {
        methods:["GET"],
        path:"/route"
    }
    hbrResource (endpoint conn, http:Request req) {
        //Create new outbound request to handle client call.
        http:Request newRequest = {};
        // Checks whether 'type' header exists in the request.
        if (!req.hasHeader("type")) {
            http:Response errorResponse = {};
            errorResponse.statusCode = 500;
            json errMsg = {"error":"'type' header not found"};
            errorResponse.setJsonPayload(errMsg);
            _ = conn -> respond(errorResponse);
            return;
        }
        //Native function getHeader() returns header value of a specified header name.
        string nameString = req.getHeader("type");

        (http:Response|http:HttpConnectorError|null) response;
        if (nameString == "location") {
            //"post" represent the POST action of HTTP connector. Route payload to relevant service.
            response = locationEP -> post("/v2/594e12271100001f13d6d3a6", newRequest);
        } else {
            //"get" action can be used to make http GET call.
            response = weatherEP -> get("/data/2.5/weather?lat=35&lon=139&appid=b1b1", newRequest);
        }

        match response {
            http:Response clientResponse => {
            //Native function "forward" sends back the inbound clientResponse to the caller if no any error is found.
                _ = conn -> forward(clientResponse);
            }
            http:HttpConnectorError err => {
                http:Response errorResponse = {};
                errorResponse.statusCode = 500;
                errorResponse.setStringPayload(err.message);
                _ = conn -> respond(errorResponse);
            }
            any => {
                http:Response errorResponse = {};
                errorResponse.statusCode = 500;
                json errMsg = {"error":"unexpected response received"};
                errorResponse.setJsonPayload(errMsg);
                _ = conn -> respond(errorResponse);
            }
        }
    }
}
