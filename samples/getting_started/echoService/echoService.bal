import ballerina.net.http;

@http:configuration {basePath:"/echo"}
service<http> echo {

    @http:resourceConfig {
        methods:["POST"],
        path:"/"
    }
    resource echo (message m) {
        http:convertToResponse(m);
        reply m;
    
    }
    
}
