import ballerina.net.http;
import ballerina.lang.messages;

@http:configuration {
    basePath:"/echo",
    port:9094
}
service<http> echo {

    @http:resourceConfig {
        methods:["POST"],
        path:"/"
    }
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

    @http:resourceConfig {
        methods:["POST"],
        path:"/abc"
    }
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

    @http:resourceConfig {
        methods:["POST"],
        path:"/"
    }
    resource echoDummy (message m) {
        message resp = {};
        messages:setStringPayload(resp, "hello world");
        reply resp;

    }

}
