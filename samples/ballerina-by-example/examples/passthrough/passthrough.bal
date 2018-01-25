import ballerina.net.http;

service<http> passthrough {

    endpoint<http:HttpClient> endPoint {
        create http:HttpClient("http://localhost:9090/echo", {});
    }

    @Description {value:"Any type of HTTP requests will be dispatchd to the passthrough resource."}
    @http:resourceConfig {
        path:"/"
    }
    resource passthrough (http:Request req, http:Response res) {
        http:Response clientResponse = {};
        http:HttpConnectorError err;
        // The forward() action can be used to forward an incoming HTTP request as-is to an upstream service.
        clientResponse, err = endPoint.forward("/", req);

        if (err != null) {
            res.setStatusCode(500);
            res.setStringPayload(err.msg);
            _ = res.send();
        } else {
            //The forward() function (bound to the Response struct) can be used to send back the incoming response(as-is) for the request we forwarded to the upstream service earlier.
            _ = res.forward(clientResponse);
        }
    }
}

@Description {value:"Sample backend echo service."}
service<http> echo {
    @Description {value:"A common resource for POST, PUT and GET methods."}
    @http:resourceConfig {
        methods:["POST", "PUT", "GET"],
        path:"/"
    }
    resource echoResource (http:Request req, http:Response res) {
        res.setStringPayload("Resource was invoked");
        _ = res.send();
    }
}
