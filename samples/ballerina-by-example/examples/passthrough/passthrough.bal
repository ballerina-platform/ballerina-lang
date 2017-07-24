import ballerina.net.http;
import ballerina.doc;

service<http> passthrough {
    @doc:Description {value:"Requests which contain any HTTP method will be directed to passthrough resource."}
    @http:Path {value:"/"}
    resource passthrough (message m) {
        http:ClientConnector endPoint = create http:ClientConnector
                                            ("http://localhost:9092/echo");
        //Extract request method from message.
        string method = http:getMethod(m);
        //Action execute() returns the response from backend service. It includes endPoint, HTTP method, resource path and message as parameters.
        message response = http:ClientConnector
                            .execute(endPoint, method, "/", m);
        reply response;
    }
}

