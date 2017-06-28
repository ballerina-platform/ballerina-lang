import ballerina.net.http;

@http:BasePath {value:"/echo"}
service echo {
    
    @http:POST{}
    @http:Path {value:"/"}
    resource echo (message m) {
        //convertToResponse is a function in the HTTP package that converts the request message into a response message.
        http:convertToResponse(m);
        reply m;
    
    }
    
}
