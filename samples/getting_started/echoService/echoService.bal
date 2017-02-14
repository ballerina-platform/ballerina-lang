import ballerina.net.http;

@http:BasePath ("/echo")
service echo {


    resource echo (message m) {
        http:convertToResponse(m);
        reply m;
    }
}
