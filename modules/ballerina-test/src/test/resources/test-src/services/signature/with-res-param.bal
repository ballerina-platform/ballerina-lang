import ballerina.net.http;

@http:configuration {basePath:"/signature"}
service<http> echo {
    resource echo1 (http:Connection conn, http:InResponse res ) {
        http:OutResponse resp = {};
    }
}
