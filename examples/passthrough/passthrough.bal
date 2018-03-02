import ballerina.net.http;

service<http> passthrough {

    @Description {value:"Requests which contain any HTTP method will be directed to passthrough resource."}
    @http:resourceConfig {
        path:"/"
    }
    resource passthrough (http:Connection conn, http:InRequest req) {
        endpoint<http:HttpClient> endPoint {
            create http:HttpClient("http://localhost:9090/echo", {});
        }
        //Extract HTTP method from the inbound request.
        string method = req.method;
        http:InResponse clientResponse = {};
        http:HttpConnectorError err;
        //Action forward() does a backend client call and returns the response. It includes endPoint, resource path and inbound request as parameters.
        clientResponse, err = endPoint.forward("/", req);
        //Native function "forward" sends back the clientResponse to the caller if no any error is found.
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

@Description {value:"Sample backend echo service."}
service<http> echo {
    @Description {value:"A common resource for POST, PUT and GET methods."}
    @http:resourceConfig {
        methods:["POST", "PUT", "GET"],
        path:"/"
    }
    resource echoResource (http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};
        res.setStringPayload("Resource is invoked");
        _ = conn.respond(res);
    }
}
