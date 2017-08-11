import ballerina.lang.messages;
import ballerina.net.http;
import ballerina.doc;

service<http> passthrough {
    @doc:Description {value:"Requests which contain any HTTP method will be directed to passthrough resource."}
    @http:Path {value:"/"}
    resource passthrough (message m) {
        http:ClientConnector endPoint = create http:ClientConnector
                                            ("http://localhost:9090/echo");
        //Extract request method from message.
        string method = http:getMethod(m);
        //Action execute() returns the response from backend service. It includes endPoint, HTTP method, resource path and message as parameters.
        message response = http:ClientConnector
                            .execute(endPoint, method, "/", m);
        reply response;
    }
}

@doc:Description {value:"Sample backend echo service."}
service<http> echo {
    @doc:Description {value:"A common resource for POST, PUT and GET methods."}
    @http:POST {}
    @http:PUT {}
    @http:GET {}
    @http:Path {value:"/"}
    resource echoResource (message m) {
        message response = {};
        messages:setStringPayload(response, "Resource is invoked");
        reply response;
    }
}

