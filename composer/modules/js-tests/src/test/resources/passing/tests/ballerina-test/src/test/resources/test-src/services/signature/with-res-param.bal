import ballerina.net.http;

@http:configuration {basePath:"/signature"}
service<http> echo {
    resource echo1 (http:Connection conn, http:Response res) {
        http:Response resp = {};
    }
}
