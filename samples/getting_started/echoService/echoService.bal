import ballerina.net.http;
@http:BasePath ("/echo")
service echo {
    
    @http:POST
    resource echo (message m) {
        http:convertToResponse(m);
        reply m;
    
    }
    
}
