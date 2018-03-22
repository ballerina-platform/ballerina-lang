import ballerina/net.http;

endpoint http:ServiceEndpoint passthroughEP {
    port:9090
};

endpoint http:ClientEndpoint clientEP {
    targets:[{uri:"http://localhost:9092/echo"}]
};

@http:ServiceConfig
service<http:Service> passthrough bind passthroughEP {

    @Description {value:"Requests which contain any HTTP method will be directed to passthrough resource."}
    @http:ResourceConfig {
        path:"/"
    }
    passthrough (endpoint outboundEP, http:Request req) {
        //Extract HTTP method from the inbound request.
        string method = req.method;
        //Action forward() does a backend client call and returns the response.
        //It includes endPoint, resource path and inbound request as parameters.
        var clientResponse = clientEP -> forward("/", req);
        //Native function "forward" sends back the clientResponse to the caller if no any error is found.
        match clientResponse {
            http:Response res => {
                _ = outboundEP -> forward(res);
            }
            http:HttpConnectorError err => {
                http:Response res = {};
                res.statusCode = 500;
                res.setStringPayload(err.message);
                _ = outboundEP -> respond(res);
            }
        }
    }
}

endpoint http:ServiceEndpoint echoEP {
    port:9092
};

@Description {value:"Sample backend echo service."}
@http:ServiceConfig
service<http:Service> echo bind echoEP {
    @Description {value:"A common resource for POST, PUT and GET methods."}
    @http:ResourceConfig {
        methods:["POST", "PUT", "GET"],
        path:"/"
    }
    echoResource (endpoint outboundEP, http:Request req) {
        http:Response res = {};
        res.setStringPayload("Resource is invoked");
        _ = outboundEP -> respond(res);
    }
}
