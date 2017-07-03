import ballerina.net.http;

@http:config{basePath:"/echo"}
service<http> echo {

    @http:POST {}
    @http:Path {value : "/message"}
    resource echo (message m) {
        reply m;
    }

}
