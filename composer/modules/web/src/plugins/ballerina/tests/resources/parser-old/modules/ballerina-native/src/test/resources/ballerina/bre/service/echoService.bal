import ballerina/http;

@http:configuration{basePath:"/echo"}
service<http> echo {

    @http:resourceConfig {
        methods:["POST"],
        path:"/message"
    }
    resource echo (message m) {
        response:send(m);
    }

}
