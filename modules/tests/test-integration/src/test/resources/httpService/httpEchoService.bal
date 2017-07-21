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

@http:configuration {
    basePath:"/echoOne",
    port:9094
}
service<http> echoOne {

    @http:POST{}
    @http:Path {value:"/abc"}
    resource echoAbc (message m) {
        message resp = {};
        messages:setStringPayload(resp, "hello world");
        reply resp;

    }
}

@http:configuration {
    basePath:"/echoDummy"
}
service<http> echoDummy {

    @http:POST{}
    @http:Path {value:"/"}
    resource echoDummy (message m) {
        message resp = {};
        messages:setStringPayload(resp, "hello world");
        reply resp;

    }

}
