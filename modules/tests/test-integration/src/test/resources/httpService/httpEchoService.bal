import ballerina.net.http;
import ballerina.lang.messages;

@http:configuration {
    basePath:"/echo",
    port:9094
}
service<http> echo {
    
    @http:POST{}
    @http:Path {value:"/"}
    resource echo (message m) {
        message resp = {};
        messages:setStringPayload(resp, "hello world");
        reply resp;
    
    }
    
}
