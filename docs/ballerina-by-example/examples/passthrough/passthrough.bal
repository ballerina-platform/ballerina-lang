import ballerina/net.http;

endpoint http:ServiceEndpoint passthroughEP {
    port:9092
};

endpoint http:ClientEndpoint clientEP {
    targets: [{uri: "http://localhost:9090/echo"}]
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
        http:Response clientResponse = {};
        http:HttpConnectorError err = {};
        //Action forward() does a backend client call and returns the response. It includes endPoint, resource path and inbound request as parameters.
        var clientRes = clientEP -> forward("/", req);
        match clientRes {
            http:Response res => {
                clientResponse = res    ;
            }
            http:HttpConnectorError connErr => {
                err = connErr;
            }
        }
        //Native function "forward" sends back the clientResponse to the caller if no any error is found.
        http:Response res = {};
        if (err != null) {
            res.statusCode = 500;
            res.setStringPayload(err.message);
            _ = outboundEP -> respond(res);
        } else {
            _ = outboundEP -> forward(clientResponse);
        }
    }
}

endpoint http:ServiceEndpoint echoEP {
    port:9090
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
