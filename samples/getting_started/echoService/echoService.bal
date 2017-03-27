import ballerina.net.http;

@http:BasePath {value:"/echo"}
service echo {
    
    @http:POST{}
    resource echo (message m) {
        http:convertToResponse(m);
        reply m;
    
    }
    
}
