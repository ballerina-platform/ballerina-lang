import ballerina.net.http;

@http:BasePath {value:"/echo"}
service echo {
    
    @http:POST{}
    @http:Path {value:"/"}
    resource echo (message m) {
        http:convertToResponse(m);
        reply m;
    
    }

    @http:POST{}
    @http:Path {value:"/*"}
    resource echoTwo (message m) {
        http:convertToResponse(m);
        reply m;

    }
    
}
