import ballerina.net.http;

service<http> passthrough {

    @Description {value:"Requests which contain any HTTP method will be directed to passthrough resource."}
    @http:resourceConfig {
        path:"/"
    }
    resource passthrough (http:Connection con, http:Request req) {
        endpoint<http:HttpClient> endPoint {
            create http:HttpClient("http://localhost:9090/echo", {});
        }
        //Extract HTTP method from the inbound request.
        string method = req.getMethod();
        http:Response clientResponse = {};
        http:HttpConnectorError err;
        //Action execute() does a backend client call and returns the response. It includes endPoint, HTTP method, resource path and request as parameters.
        clientResponse, err = endPoint.execute(method, "/", req);
        //Native function "respond" sends back the clientResponse to the caller if no any error is found.
        http:Response res = {};
        if (err != null) {
            res.setStatusCode(500);
            res.setStringPayload(err.msg);
            _ = con.respond(res);
        } else {
            _ = con.respond(clientResponse);
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
    resource echoResource (http:Connection con, http:Request req) {
        http:Response res = {};
        res.setStringPayload("Resource is invoked");
        _ = con.respond(res);
    }
}
