import ballerina.net.http;
import ballerina.lang.messages;
import ballerina.doc;

@doc:Description {value:"Service will be deployed on port 9092."}
@http:configuration {
    basePath:"/echo",
    port:9092
}
service<http> echoService {
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

