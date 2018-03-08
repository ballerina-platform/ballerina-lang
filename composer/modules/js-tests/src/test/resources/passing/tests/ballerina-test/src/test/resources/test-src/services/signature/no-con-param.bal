import ballerina.net.http;

@http:configuration {basePath:"/signature"}
service<http> echo {
    resource echo1 (http:InRequest req, http:InResponse res) {
        http:OutResponse resp = {};
    }
}
