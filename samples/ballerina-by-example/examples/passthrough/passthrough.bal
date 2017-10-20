import ballerina.net.http;
import ballerina.doc;

service<http> passthrough {
    @doc:Description {value:"Requests which contain any HTTP method will be directed to passthrough resource."}
    @http:resourceConfig {
        path:"/"
    }
    resource passthrough (http:Request req, http:Response res) {
        http:ClientConnector endPoint = create http:ClientConnector
                                        ("http://localhost:9090/echo", {});
        //Extract request method from message.
        string method = req.getMethod();
        //Action execute() returns the response from backend service. It includes endPoint, HTTP method, resource path and message as parameters.
        res = endPoint.execute(method, "/", req);
        res.send();
    }
}

@doc:Description {value:"Sample backend echo service."}
service<http> echo {
    @doc:Description {value:"A common resource for POST, PUT and GET methods."}
    @http:resourceConfig {
        methods:["POST", "PUT", "GET"],
        path:"/"
    }
    resource echoResource (http:Request req, http:Response res) {
        res.setStringPayload("Resource is invoked");
        res.send();
    }
}
