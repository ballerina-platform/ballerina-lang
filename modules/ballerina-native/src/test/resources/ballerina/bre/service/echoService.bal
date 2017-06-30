import ballerina.net.http;

@http:BasePath {value:"/echo"}
service echo {

    @http:POST {}
    @http:Path {value : "/message"}
    resource echo (message m) {
        reply m;
    }

}
