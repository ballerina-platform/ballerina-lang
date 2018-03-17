import ballerina.net.http;

endpoint<http:Service> passthroughEP {
    port:9092
}

endpoint<http:Client> clientEP {
    serviceUri: "http://localhost:9090/echo"
}

@http:serviceConfig { endpoints:[passthroughEP] }
service<http:Service> passthrough {

    @Description {value:"Requests which contain any HTTP method will be directed to passthrough resource."}
    @http:resourceConfig {
        path:"/"
    }
    resource passthrough (http:ServerConnector conn, http:Request req) {
        //Extract HTTP method from the inbound request.
        string method = req.method;
        http:Response clientResponse = {};
        http:HttpConnectorError err;
        //Action forward() does a backend client call and returns the response. It includes endPoint, resource path and inbound request as parameters.
        clientResponse, err = clientEP -> forward("/", req);
        //Native function "forward" sends back the clientResponse to the caller if no any error is found.
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

endpoint<http:Service> echoEP {
    port:9090
}

@Description {value:"Sample backend echo service."}
@http:serviceConfig { endpoints:[echoEP] }
service<http:Service> echo {
    @Description {value:"A common resource for POST, PUT and GET methods."}
    @http:resourceConfig {
        methods:["POST", "PUT", "GET"],
        path:"/"
    }
    resource echoResource (http:ServerConnector conn, http:Request req) {
        http:Response res = {};
        res.setStringPayload("Resource is invoked");
        _ = conn -> respond(res);
    }
}
