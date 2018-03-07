import ballerina.net.http;

@http:configuration {basePath:"/signature"}
service<http> echo {
    resource echo1 (http:Connection conn, int key, http:InRequest req ) {
        http:OutResponse res = {};
    }
}
