import ballerina.net.http;

@http:config {
    basePath:"/echo",
    port:9094
}
service<http> echo {
    
    @http:POST{}
    @http:Path {value:"/"}
    resource echo (message m) {
        http:convertToResponse(m);
        reply m;
    
    }
    
}
