import ballerina.net.http;

service<http> passthrough {

    @Description {value:"Requests which contain any HTTP method will be directed to passthrough resource."}
    @http:resourceConfig {
        path:"/"
    }
    resource passthrough (http:Request req, http:Response res) {
        http:ClientConnector endPoint = create http:ClientConnector
                                        ("http://localhost:9090/echo", {});
        //Extract HTTP method from the inbound request.
        string method = req.getMethod();
        http:Response clientResponse = {};
        //Action execute() does a backend client call and returns the response. It includes endPoint, HTTP method, resource path and request as parameters.
        clientResponse = endPoint.execute(method, "/", req);
        //Native function "forward" sends back the clientResponse to the caller.
        res.forward(clientResponse);
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
        res.setStringPayload("Resource is invoked");
        res.send();
    }
}
